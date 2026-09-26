$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot

if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME) -or
    -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $javaRoot = Join-Path $env:ProgramFiles 'Java'
    $latestJdk = Get-ChildItem -LiteralPath $javaRoot -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin\java.exe') } |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($null -ne $latestJdk) {
        $env:JAVA_HOME = $latestJdk.FullName
    }
    else {
        throw 'Không tìm thấy JDK hợp lệ. Hãy cài JDK 21 và đặt JAVA_HOME trước khi chạy.'
    }
}

Push-Location (Join-Path $repositoryRoot 'backend')
try {
    mvn -DskipTests install
    mvn install:install-file `
        -Dfile="target/backend-0.0.1-SNAPSHOT.jar.original" `
        -DgroupId=com.fpt `
        -DartifactId=backend `
        -Dversion=0.0.1-SNAPSHOT `
        -Dpackaging=jar `
        -Dclassifier=classes `
        -DpomFile="pom.xml"
}
finally {
    Pop-Location
}

Push-Location (Join-Path $PSScriptRoot 'backend-integration')
try {
    mvn verify
}
finally {
    Pop-Location
}

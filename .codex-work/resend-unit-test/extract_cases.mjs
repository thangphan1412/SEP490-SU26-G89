import fs from "node:fs";

const source = fs.readFileSync(new URL("./build_report.mjs", import.meta.url), "utf8");
const match = source.match(/const functions = (\[[\s\S]*?\n\]);\n\nconst blank/);
if (!match) throw new Error("Unable to locate function test definitions.");
const definitions = Function(`"use strict"; return (${match[1]});`)();
process.stdout.write(JSON.stringify(definitions));

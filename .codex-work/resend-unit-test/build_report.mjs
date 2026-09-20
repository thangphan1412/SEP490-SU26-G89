import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const inputPath = "C:/Users/acer/Downloads/Report5.1_Unit Test.xlsx";
const outputDir = "../../outputs/01a0a602-3ae5-73c1-8485-d4f67d55a97c";
const outputPath = path.join(outputDir, "Report5.1_Unit_Test.xlsx");
const previewDir = path.join(outputDir, "previews");
const executedDate = new Date(Date.UTC(2026, 8, 16));

const functions = [
  { sheet:"getAllUsers", module:"User Management", method:"getAllUsersFiltered", screen:"List User", requirement:"Verify authorized listing, filter normalization, paging, role scope, DTO mapping, and error handling.", cases:[
    ["Authorized principal requests user page","HTTP 200 with requested Page<UserResponseDTO>","N"],
    ["Service rejects invalid list filter","HTTP 400 and response data is null","A"],
    ["Repository returns employee and partner","All users are mapped to response DTOs","N"],
    ["Administrator uses All filters; page 1 size 20","Filters normalized and pageable sorts id DESC","N"],
    ["HOD requests users from another department","Own department and Employee role are enforced","A"],
    ["Employee role requests the user list","Access Denied; repository search is not called","A"],
    ["Principal email is not found","Current user not found; repository search is not called","A"],
  ]},
  { sheet:"createUser", module:"User Management", method:"createUser", screen:"Create User", requirement:"Verify creation response, required password, duplicate email, role/department authorization, role assignment, and welcome email.", cases:[
    ["Controller receives a valid create request","HTTP 201 with the created user","N"],
    ["Service rejects the create request","HTTP 400 with service error message","A"],
    ["Password is blank","Rejected before any repository access","B"],
    ["Email already exists","Rejected as duplicate; user is not saved","A"],
    ["CEO attempts to create a user","Access Denied; user is not saved","A"],
    ["Accountant attempts to create Administrator","Access Denied; user is not saved","A"],
    ["Accountant creates Employee in a department","User, role, department, and welcome email are handled","N"],
    ["HOD creates Employee in own department","User is created successfully","N"],
    ["HOD creates Employee outside own department","Access Denied; user is not saved","A"],
    ["A second active HOD is created in one department","Rejected; existing active HOD remains unique","A"],
  ]},
  { sheet:"getUserById", module:"User Management", method:"getUserById", screen:"View User", requirement:"Verify user detail mapping, not-found behavior, and role/department access rules.", cases:[
    ["Controller requests an existing user ID","HTTP 200 with the user details","N"],
    ["Controller requests an unknown user ID","HTTP 404 and response data is null","A"],
    ["Accountant views an Employee","User details and department are returned","N"],
    ["CEO views another CEO","Access Denied","A"],
    ["HOD views Employee in own department","User details are returned","N"],
    ["HOD views Employee in another department","Access Denied","A"],
    ["Repository cannot find the target ID","User not found; current user is not queried","A"],
  ]},
  { sheet:"updateUser", module:"User Management", method:"updateUser", screen:"Update User", requirement:"Verify update response, target existence, authorization, duplicate email, password encoding, department update, and role assignment.", cases:[
    ["Controller receives a permitted update","HTTP 200 with updated user","N"],
    ["Service rejects the update","HTTP 400 with service error message","A"],
    ["Accountant updates Employee with new password","Fields update and password is encoded","N"],
    ["Target user ID does not exist","User not found; update is not persisted","A"],
    ["Administrator attempts to update a user","Access Denied; update is not persisted","A"],
    ["Accountant changes Employee to Administrator","Access Denied; update is not persisted","A"],
    ["HOD updates Employee in own department","Status and department are updated","N"],
    ["HOD updates Employee in another department","Access Denied; update is not persisted","A"],
    ["Email is changed to one already in use","Duplicate email is rejected","A"],
    ["Role changes and assignment already exists","Existing UserRole is replaced and saved","N"],
    ["Role changes and no assignment exists","New UserRole is created and saved","N"],
  ]},
  { sheet:"getCompanyProfile", module:"Company Profile Management", method:"getCompanyProfile", screen:"View Company Profile", requirement:"Verify company profile retrieval, response mapping, and missing-company handling.", cases:[
    ["Controller requests the company profile","HTTP 200 with company profile","N"],
    ["Profile service cannot find the company","HTTP 404 and response data is null","A"],
    ["Company entity exists","Identity name and registration number are mapped","N"],
    ["Company repository returns empty","Company not found is raised","A"],
  ]},
  { sheet:"updateCompanyProfile", module:"Company Profile Management", method:"updateCompanyProfile", screen:"Update Company Profile", requirement:"Verify editable company fields, registration number mapping, persistence order, optional notification, and error handling.", cases:[
    ["Controller receives a valid company update","HTTP 200 with updated profile","N"],
    ["Profile service rejects the update","HTTP 400 and response data is null","A"],
    ["Name and registration number are updated","Fields are mapped and persisted","N"],
    ["Notification email is provided","Update notification is sent","N"],
    ["Notification email is null","Update succeeds and notification is skipped","B"],
    ["Notification email is empty","Update succeeds and notification is skipped","B"],
    ["Notification service fails","Persisted changes are returned","A"],
    ["Company does not exist","Rejected before save or email","A"],
    ["Repository save returns a managed entity","Service returns the saved entity mapping","N"],
    ["Valid update includes notification","Repository save occurs before email","N"],
  ]},
  { sheet:"getMyProfile", module:"User Profile Management", method:"getMyProfile", screen:"View User Profile", requirement:"Verify current-user resolution, profile mapping, CEO department visibility, and failure responses.", cases:[
    ["Authenticated user requests own profile","HTTP 200 with current user's profile","N"],
    ["Current user cannot be resolved","HTTP 400 with error message","A"],
    ["Profile service fails","HTTP 400 with error message","A"],
    ["Employee profile and support fields exist","All response fields are mapped","N"],
    ["CEO profile is requested","Profile is returned without department","N"],
    ["User ID does not exist","User not found is raised","A"],
  ]},
  { sheet:"updateMyProfile", module:"User Profile Management", method:"updateMyProfile", screen:"Update User Profile", requirement:"Verify self-update ID binding, editable fields, email uniqueness, notification content, and failure handling.", cases:[
    ["Authenticated user submits profile update","Current user's ID is used; HTTP 200","N"],
    ["Requested email belongs to another account","HTTP 400 duplicate-email response","A"],
    ["Current user cannot be resolved","HTTP 400; service is not called","A"],
    ["Editable fields are changed","Role, status, and department remain unchanged","N"],
    ["Email value is unchanged","Duplicate-email lookup is skipped","N"],
    ["Email changes to an available address","Profile saves and notification includes change","N"],
    ["Email changes to a duplicate address","Update is rejected and not saved","A"],
    ["Unchanged email with successful update","Notification omits email-change notice","B"],
    ["Notification service fails after save","Saved profile is still returned","A"],
    ["Target user does not exist","Rejected and notification is not sent","A"],
  ]},
  { sheet:"getOverviewStatistics", module:"Dashboard Management", method:"getOverviewStatistics", screen:"View Contracts Statistics", requirement:"Verify dashboard totals, status distribution, percentages, six-month trend, upcoming expirations, and fallback handling.", cases:[
    ["Controller requests dashboard overview","HTTP 200 with overview DTO","N"],
    ["Overview service raises an exception","Exception propagates to global handler","A"],
    ["Contracts and upcoming expirations exist","Metrics, charts, trend, and top five are built","N"],
    ["No contracts exist","Percentages are 0.0% and series stay valid","B"],
    ["Status value is null or unknown","UNKNOWN label and fallback color are used","A"],
  ]},
  { sheet:"getContracts", module:"Contract Management", method:"getContracts", screen:"View Total Contracts", requirement:"Verify total-contract list query mapping, default paging/sorting, empty results, cache control, and exception propagation.", cases:[
    ["Search, status, page, sort, and direction are provided","All parameters reach service; HTTP 200 no-store","N"],
    ["Controller default query values are used","Empty filters, page 0, id DESC reach service","B"],
    ["Search returns no contracts","Empty page is returned and not converted to null","B"],
    ["Service rejects an invalid status","Service exception is propagated","A"],
  ]},
  { sheet:"getPendingSignatureDashboard", module:"Dashboard Management", method:"getPendingSignatureDashboard", screen:"View Pending Signature Contracts", requirement:"Verify pending totals, aging buckets, overdue/due-soon counts, project grouping, averages, and empty/null handling.", cases:[
    ["Controller requests pending-signature dashboard","HTTP 200 with dashboard DTO","N"],
    ["No pending contracts exist","Zero metrics and empty project chart are returned","B"],
    ["Pending contracts span age ranges and projects","Ages, deadlines, average, and projects are grouped","N"],
    ["Project or createdAt is null","General project fallback; null date adds no days","A"],
  ]},
  { sheet:"getStatisticalReports", module:"Dashboard Management", method:"getStatisticalReports", screen:"View Contract Statistical Reports", requirement:"Verify report response, contract-type distribution, percentages, colors, totals, and reuse of overview datasets.", cases:[
    ["Controller requests statistical reports","HTTP 200 with statistical report DTO","N"],
    ["Contract types and overview data exist","Distributions, colors, totals, and overview data are built","N"],
  ]},
];

const blank = (r,c) => Array.from({length:r},()=>Array(c).fill(null));
const put = (s,a,v) => { s.getRange(a).values=[[v]]; };

function fillFunctionSheet(sheet, config) {
  const compact = config.sheet === "getContracts";
  const confirmRow = compact ? 30 : 31;
  const resultRow = compact ? 38 : 43;
  const passedRow = resultRow + 1;
  const dateRow = resultRow + 2;
  const defectRow = resultRow + 3;
  put(sheet,"C1",config.module); put(sheet,"L1",config.method);
  put(sheet,"C2","Project team"); put(sheet,"L2","Codex / Maven"); put(sheet,"C3",config.requirement);
  sheet.getRange("B8:E42").clear({applyTo:"contents"});
  sheet.getRange("F7:T46").clear({applyTo:"contents"});
  put(sheet,"A8","Condition"); put(sheet,"B8","Precondition"); put(sheet,"C8","Scenario / Input");
  put(sheet,"B9","Setup"); put(sheet,"B10","Test case");
  put(sheet,`A${confirmRow}`,"Confirm"); put(sheet,"B31","Return"); put(sheet,"C31","Expected result"); put(sheet,"B32","Expected");
  put(sheet,`A${resultRow}`,"Result"); put(sheet,`B${resultRow}`,"Type(N : Normal, A : Abnormal, B : Boundary)");
  put(sheet,`B${passedRow}`,"Passed/Failed"); put(sheet,`B${dateRow}`,"Executed Date"); put(sheet,`B${defectRow}`,"Defect ID");
  sheet.getRange("A5").formulas=[[`=COUNTIF(F${passedRow}:T${passedRow},\"P\")`]];
  sheet.getRange("C5").formulas=[[`=COUNTIF(F${passedRow}:T${passedRow},\"F\")`]];
  sheet.getRange("F5").formulas=[["=O5-A5-C5"]];
  sheet.getRange("L5").formulas=[[`=COUNTIF(F${resultRow}:T${resultRow},\"N\")`]];
  sheet.getRange("M5").formulas=[[`=COUNTIF(F${resultRow}:T${resultRow},\"A\")`]];
  sheet.getRange("N5").formulas=[[`=COUNTIF(F${resultRow}:T${resultRow},\"B\")`]];
  sheet.getRange("O5").formulas=[["=COUNTA(F7:T7)"]];
  const ids=Array(15).fill(null), conditions=Array(16).fill(null), cm=blank(16,15), expected=Array(11).fill(null), em=blank(11,15), types=Array(15).fill(null), results=Array(15).fill(null), dates=Array(15).fill(null);
  conditions[0]="JUnit 5 + Mockito; dependencies mocked";
  config.cases.forEach(([scenario,outcome,type],i)=>{ids[i]=`UTCID${String(i+1).padStart(2,"0")}`;cm[0][i]="O";conditions[i+1]=scenario;cm[i+1][i]="O";expected[i]=outcome;em[i][i]="O";types[i]=type;results[i]="P";dates[i]=executedDate;});
  sheet.getRange("F7:T7").values=[ids]; sheet.getRange("C9:C24").values=conditions.map(v=>[v]); sheet.getRange("F9:T24").values=cm;
  sheet.getRange("C32:C42").values=expected.map(v=>[v]); sheet.getRange("F32:T42").values=em; sheet.getRange(`F${resultRow}:T${dateRow}`).values=[types,results,dates];
  sheet.getRange("F7:T7").format.font={name:"Arial",size:6,bold:true,color:"#FFFFFF"};
  sheet.getRange("C3").format.font={name:"Arial",size:7,italic:true,color:"#000000"}; sheet.getRange("C3").format.wrapText=true; sheet.getRange("A3:T3").format.rowHeight=30;
  for(const r of ["C9:C24","C32:C42"]){sheet.getRange(r).format.font={name:"Arial",size:7,color:"#000000"};sheet.getRange(r).format.wrapText=true;sheet.getRange(r).format.horizontalAlignment="left";sheet.getRange(r).format.verticalAlignment="center";}
  sheet.getRange("A9:T24").format.rowHeight=36; sheet.getRange("A32:T42").format.rowHeight=36;
  sheet.getRange(`F${dateRow}:T${dateRow}`).format.numberFormat="d/m/yy"; sheet.getRange(`F${dateRow}:T${dateRow}`).format.font={name:"Arial",size:6,color:"#000000"};
}

const workbook=await SpreadsheetFile.importXlsx(await FileBlob.load(inputPath));
const originalNames=workbook.worksheets.items.map(s=>s.name);
for(const config of functions) fillFunctionSheet(workbook.worksheets.getItem(config.sheet),config);

const cover=workbook.worksheets.getItem("Cover"); put(cover,"F5",executedDate); put(cover,"F6",4); cover.getRange("A14:F14").values=[[executedDate,4,"Unit test scope","A","Add executed unit tests for 12 requested screens","Report5.1_Unit Test"]];
const methodList=workbook.worksheets.getItem("MethodList"); put(methodList,"C6","Java 21; Maven 3.9.2; JUnit 5; Mockito; Spring Boot 4.0.6; Windows 11; source snapshot 1164f260.");
methodList.getRange("A9:F20").clear({applyTo:"contents"}); methodList.getRange("A9:F20").values=functions.map((c,i)=>[i+1,c.module,`${c.method}()`,c.sheet,c.screen,"JUnit 5; Mockito mocks; authenticated context where required"]); methodList.getRange("C9:D20").format.font={name:"Arial",size:6,color:"#000000"};

const stats=workbook.worksheets.getItem("Statistics"); put(stats,"F4","Codex / Maven"); put(stats,"F6",executedDate); put(stats,"B7","80/80 tests passed on source snapshot 1164f260. Current working tree compile is blocked by unresolved merge conflicts."); stats.getRange("A12:I22").clear({applyTo:"contents"});
stats.getRange("A12:B15").values=[[1,"User Mgmt (4)"],[2,"User Profile (2)"],[3,"Company Profile (2)"],[4,"Dashboard + Contract (4)"]];
const groups=[["getAllUsers","createUser","getUserById","updateUser"],["getMyProfile","updateMyProfile"],["getCompanyProfile","updateCompanyProfile"],["getOverviewStatistics","getContracts","getPendingSignatureDashboard","getStatisticalReports"]];
const cells=["A5","C5","F5","L5","M5","N5","O5"];
stats.getRange("C12:I15").formulas=groups.map(g=>cells.map(cell=>`=SUM(${g.map(n=>`'${n}'!${cell}`).join(",")})`)); put(stats,"B16","Sub total"); stats.getRange("C16:I16").formulas=[["=SUM(C12:C15)","=SUM(D12:D15)","=SUM(E12:E15)","=SUM(F12:F15)","=SUM(G12:G15)","=SUM(H12:H15)","=SUM(I12:I15)"]];
for(const [row,label] of [[18,"Test coverage"],[19,"Test successful coverage"],[20,"Normal case"],[21,"Abnormal case"],[22,"Boundary case"]]) put(stats,`B${row}`,label);
stats.getRange("D18:D22").formulas=[["=IF(I16=0,0,(C16+D16)/I16*100)"],["=IF(I16=0,0,C16/I16*100)"],["=IF(I16=0,0,F16/I16*100)"],["=IF(I16=0,0,G16/I16*100)"],["=IF(I16=0,0,H16/I16*100)"]]; stats.getRange("E18:E22").values=[["%"],["%"],["%"],["%"],["%"]];

workbook.recalculate();
if(JSON.stringify(originalNames)!==JSON.stringify(workbook.worksheets.items.map(s=>s.name))) throw new Error("Template sheet order changed");
const summary=await workbook.inspect({kind:"table",range:"Statistics!A11:I22",include:"values,formulas",tableMaxRows:20,tableMaxCols:12,maxChars:5000}); console.log(`SUMMARY\n${summary.ndjson}`);
const errors=await workbook.inspect({kind:"match",searchTerm:"#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!",options:{useRegex:true,maxResults:300},summary:"final formula error scan"}); if(!errors.ndjson.includes("matched 0 entries")&&!errors.ndjson.includes('"matches":0')) throw new Error(errors.ndjson);
await fs.mkdir(previewDir,{recursive:true}); const targets=[["Cover","A1:F14"],["MethodList","A1:F20"],["Statistics","A1:I40"],...functions.map(c=>[c.sheet,"A1:T46"])]; for(const [sheetName,range] of targets){const b=await workbook.render({sheetName,range,scale:1.25,format:"png"});await fs.writeFile(path.join(previewDir,`${sheetName}.png`),new Uint8Array(await b.arrayBuffer()));}
await fs.mkdir(outputDir,{recursive:true}); const output=await SpreadsheetFile.exportXlsx(workbook); await output.save(outputPath);
const reopened=await SpreadsheetFile.importXlsx(await FileBlob.load(outputPath)); if(JSON.stringify(originalNames)!==JSON.stringify(reopened.worksheets.items.map(s=>s.name))) throw new Error("Saved workbook sheet order changed");
const finalCheck=await reopened.inspect({kind:"table",range:"Statistics!A11:I22",include:"values,formulas",tableMaxRows:20,tableMaxCols:12,maxChars:5000}); console.log(`FINAL\n${finalCheck.ndjson}`); console.log(`OUTPUT ${path.resolve(outputPath)}`);

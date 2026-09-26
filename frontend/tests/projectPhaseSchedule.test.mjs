import assert from "node:assert/strict";
import test from "node:test";
import {
  getPhaseDateError,
  getPhaseStartMinDate,
} from "../src/components/projectComponents/projectFormUtils.js";

const projectStart = "2026-10-01";
const projectEnd = "2026-10-31";
const phase = (startDate, endDate) => ({ startDate, endDate });
const firstPhase = phase("2026-10-01", "2026-10-10");
const validate = (phases) => getPhaseDateError(phases, projectStart, projectEnd);

for (const startDate of ["2026-10-05", "2026-10-10"]) {
  test("rejects a later phase starting on " + startDate + " before the previous phase is finished", () => {
    assert.equal(
      validate([firstPhase, phase(startDate, "2026-10-20")]),
      "Phase 2 start date must be after phase 1 end date."
    );
  });
}

test("rejects phases supplied in reverse chronological order", () => {
  assert.equal(
    validate([phase("2026-10-12", "2026-10-15"), firstPhase]),
    "Phase 2 start date must be after phase 1 end date."
  );
});

test("checks the boundary between the second and third phases", () => {
  assert.equal(
    validate([
      firstPhase,
      phase("2026-10-11", "2026-10-20"),
      phase("2026-10-19", "2026-10-30"),
    ]),
    "Phase 3 start date must be after phase 2 end date."
  );
});

test("accepts consecutive phases, including a phase lasting one day", () => {
  assert.equal(validate([
    firstPhase,
    phase("2026-10-11", "2026-10-11"),
    phase("2026-10-12", projectEnd),
  ]), "");
});

test("accepts an ordered schedule with a gap between phases", () => {
  assert.equal(validate([firstPhase, phase("2026-10-15", "2026-10-20")]), "");
});

test("keeps phases optional", () => {
  assert.equal(validate([]), "");
});

for (const [label, invalidPhase, expected] of [
  ["missing start", phase("", "2026-10-10"), "Phase 1 start date is required."],
  ["missing end", phase(projectStart, ""), "Phase 1 end date is required."],
  ["inverted dates", phase("2026-10-11", "2026-10-10"), "Phase 1 start date must not be after its end date."],
  ["outside project start", phase("2026-09-30", "2026-10-10"), "Phase 1 start date must not be before the project start date."],
  ["outside project end", phase(projectStart, "2026-11-01"), "Phase 1 end date must not be after the project end date."],
]) {
  test("preserves validation for " + label, () => {
    assert.equal(validate([invalidPhase]), expected);
  });
}

test("uses the project start as the minimum for the first phase", () => {
  assert.equal(getPhaseStartMinDate([], 0, projectStart), projectStart);
});

test("uses the next day after the preceding phase for both an existing and a new phase", () => {
  assert.equal(getPhaseStartMinDate([firstPhase], 1, projectStart), "2026-10-11");
  assert.equal(
    getPhaseStartMinDate([firstPhase, phase("2026-10-15", "2026-10-20")], 1, projectStart),
    "2026-10-11"
  );
});

test("recalculates the minimum when the preceding phase end date changes", () => {
  assert.equal(
    getPhaseStartMinDate([phase(projectStart, "2026-10-20")], 1, projectStart),
    "2026-10-21"
  );
});

test("never sets a minimum earlier than the project start", () => {
  assert.equal(
    getPhaseStartMinDate([phase("2026-09-01", "2026-09-20")], 1, projectStart),
    projectStart
  );
});

for (const [endDate, expected] of [
  ["2026-10-31", "2026-11-01"],
  ["2026-12-31", "2027-01-01"],
  ["2028-02-28", "2028-02-29"],
  ["2026-02-28", "2026-03-01"],
]) {
  test("calculates the next calendar day after " + endDate, () => {
    assert.equal(
      getPhaseStartMinDate([phase("2026-01-01", endDate)], 1, "2026-01-01"),
      expected
    );
  });
}

test("uses the project minimum while the preceding date is empty or incomplete", () => {
  for (const endDate of ["", "invalid"]) {
    assert.equal(
      getPhaseStartMinDate([phase(projectStart, endDate)], 1, projectStart),
      projectStart
    );
  }
});

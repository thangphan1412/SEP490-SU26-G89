let localPositionSequence = 0;

export function createPositionClientId() {
    localPositionSequence += 1;
    return `template-position-${Date.now()}-${localPositionSequence}`;
}

export function cloneVersionPositions(positions = []) {
    return (Array.isArray(positions) ? positions : []).map((position) => ({
        ...position,
        clientId: createPositionClientId(),
        pageNumber: Number(position.pageNumber) || 1,
        xPosition: Number(position.xPosition) || 0,
        yPosition: Number(position.yPosition) || 0,
        width: Number(position.width) || 0.25,
        height: Number(position.height) || 0.06,
        systemField: Boolean(position.systemField),
        required: Boolean(position.required),
    }));
}

export function toPositionRequest(position) {
    return {
        attributeKey: position.attributeKey.trim().toLowerCase(),
        fieldLabel: position.fieldLabel.trim(),
        pageNumber: Number(position.pageNumber),
        xPosition: roundCoordinate(position.xPosition),
        yPosition: roundCoordinate(position.yPosition),
        width: roundCoordinate(position.width),
        height: roundCoordinate(position.height),
        fieldType: position.fieldType,
        valueSource: position.valueSource,
        signerRole: position.signerRole || null,
        systemField: Boolean(position.systemField),
        required: Boolean(position.required),
    };
}

function roundCoordinate(value) {
    return Math.round(Number(value) * 10000) / 10000;
}

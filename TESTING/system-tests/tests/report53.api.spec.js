import { expect, test } from '@playwright/test';
import {
  apiBaseUrl,
  authHeaders,
  loginApi,
  requireDestructive,
  requireEnv,
  responseBody,
  statusValue,
} from './helpers/system.js';

test.describe('Report 5.3 - deployed-system API checks', () => {
  test('@report53 E2E-01 kết quả cuối luồng có project hoàn thành, PDF và hợp đồng Ended', async ({ request }) => {
    const env = requireEnv([
      'E2E_ADMIN_EMAIL',
      'E2E_ADMIN_PASSWORD',
      'E2E_PROJECT_ID',
      'E2E_FINAL_CONTRACT_ID',
    ]);
    const token = await loginApi(request, env.E2E_ADMIN_EMAIL, env.E2E_ADMIN_PASSWORD);
    const headers = authHeaders(token);

    const projectResponse = await request.get(
      `${apiBaseUrl}/api/projects/${env.E2E_PROJECT_ID}`,
      { headers },
    );
    expect(projectResponse.ok(), await projectResponse.text()).toBeTruthy();
    const project = (await projectResponse.json()).data;
    expect(project.phases.length).toBeGreaterThanOrEqual(2);
    expect(project.phases.every((phase) => phase.status === 'COMPLETED')).toBeTruthy();

    const contractResponse = await request.get(
      `${apiBaseUrl}/api/v1/contracts/${env.E2E_FINAL_CONTRACT_ID}`,
      { headers },
    );
    expect(contractResponse.ok(), await contractResponse.text()).toBeTruthy();
    const contract = (await contractResponse.json()).data;
    expect(contract.contractStatus).toBe('ENDED');
    expect(contract.contractEndedAt).toBeTruthy();
    expect(contract.pdfAvailable).toBeTruthy();

    const pdfResponse = await request.get(
      `${apiBaseUrl}/api/v1/contracts/${env.E2E_FINAL_CONTRACT_ID}/pdf`,
      { headers },
    );
    expect(pdfResponse.ok(), await pdfResponse.text()).toBeTruthy();
    expect(pdfResponse.headers()['content-type']).toContain('application/pdf');

    const dashboardResponse = await request.get(
      `${apiBaseUrl}/api/v1/dashboard/overview`,
      { headers },
    );
    const dashboard = (await dashboardResponse.json()).data;
    expect(dashboard.expiredAgreements).toBeGreaterThanOrEqual(1);
  });

  test('@report53 ALT-02 email đã tồn tại chặn tạo tài khoản', async ({ request }) => {
    const env = requireEnv(['E2E_DUPLICATE_EMAIL']);
    const response = await request.post(`${apiBaseUrl}/api/register`, {
      data: {
        firstName: 'Report53',
        lastName: 'Duplicate',
        email: env.E2E_DUPLICATE_EMAIL,
        password: 'StrongPass1!',
      },
    });

    expect(response.status()).toBe(400);
    expect(JSON.stringify(await responseBody(response))).toMatch(/already exists|đã tồn tại/i);
  });

  test('@report53 ALT-03 xoá project dở dang làm project không còn truy cập được', async ({ request }) => {
    requireDestructive();
    const env = requireEnv([
      'E2E_ADMIN_EMAIL',
      'E2E_ADMIN_PASSWORD',
      'E2E_DELETE_PROJECT_ID',
    ]);
    const token = await loginApi(request, env.E2E_ADMIN_EMAIL, env.E2E_ADMIN_PASSWORD);
    const headers = authHeaders(token);

    const response = await request.delete(
      `${apiBaseUrl}/api/projects/${env.E2E_DELETE_PROJECT_ID}`,
      { headers },
    );
    expect(response.ok(), await response.text()).toBeTruthy();

    const afterDelete = await request.get(
      `${apiBaseUrl}/api/projects/${env.E2E_DELETE_PROJECT_ID}`,
      { headers },
    );
    expect(afterDelete.status()).toBeGreaterThanOrEqual(400);
  });

  test('@report53 ALT-04 không xoá loại hợp đồng đang được tham chiếu', async ({ request }) => {
    const env = requireEnv([
      'E2E_ADMIN_EMAIL',
      'E2E_ADMIN_PASSWORD',
      'E2E_REFERENCED_CONTRACT_TYPE_ID',
      'E2E_REFERENCING_CONTRACT_ID',
    ]);
    const token = await loginApi(request, env.E2E_ADMIN_EMAIL, env.E2E_ADMIN_PASSWORD);
    const headers = authHeaders(token);

    const deleteResponse = await request.delete(
      `${apiBaseUrl}/api/v1/contract-types/${env.E2E_REFERENCED_CONTRACT_TYPE_ID}`,
      { headers },
    );
    expect(deleteResponse.status()).toBeGreaterThanOrEqual(400);

    const contractResponse = await request.get(
      `${apiBaseUrl}/api/v1/contracts/${env.E2E_REFERENCING_CONTRACT_ID}`,
      { headers },
    );
    expect(contractResponse.ok(), await contractResponse.text()).toBeTruthy();
  });

  test('@report53 ALT-05 trưởng phòng từ chối và hợp đồng không thể sang bước ký', async ({ request }) => {
    requireDestructive();
    const env = requireEnv([
      'E2E_HEAD_EMAIL',
      'E2E_HEAD_PASSWORD',
      'E2E_REJECT_CONTRACT_ID',
    ]);
    const token = await loginApi(request, env.E2E_HEAD_EMAIL, env.E2E_HEAD_PASSWORD);
    const response = await request.post(
      `${apiBaseUrl}/api/v1/contracts/${env.E2E_REJECT_CONTRACT_ID}/transitions`,
      {
        headers: authHeaders(token),
        data: { action: 'REJECT', comment: 'Rejected by Report 5.3 ALT-05' },
      },
    );
    expect(response.ok(), await response.text()).toBeTruthy();
    const contract = (await response.json()).data;
    expect(contract.contractStatus).toBe('CANCELLED');
    expect(contract.contractCancellationReason).toMatch(/Report 5\.3|Rejected/i);
    expect(contract.workflowRuntime?.availableActions || []).not.toContain('SIGN');
  });

  test('@report53 ALT-06 huỷ hợp đồng chờ ký và giảm số liệu pending', async ({ request }) => {
    requireDestructive();
    const env = requireEnv([
      'E2E_CANCEL_ACTOR_EMAIL',
      'E2E_CANCEL_ACTOR_PASSWORD',
      'E2E_CANCEL_CONTRACT_ID',
    ]);
    const token = await loginApi(
      request,
      env.E2E_CANCEL_ACTOR_EMAIL,
      env.E2E_CANCEL_ACTOR_PASSWORD,
    );
    const headers = authHeaders(token);
    const pendingBeforeResponse = await request.get(
      `${apiBaseUrl}/api/v1/dashboard/pending-signatures`,
      { headers },
    );
    const pendingBefore = (await pendingBeforeResponse.json()).data.totalPending;

    const cancelResponse = await request.post(
      `${apiBaseUrl}/api/v1/contracts/${env.E2E_CANCEL_CONTRACT_ID}/transitions`,
      {
        headers,
        data: { action: 'CANCEL', comment: 'Cancelled by Report 5.3 ALT-06' },
      },
    );
    expect(cancelResponse.ok(), await cancelResponse.text()).toBeTruthy();
    expect((await cancelResponse.json()).data.contractStatus).toBe('CANCELLED');

    const pendingAfterResponse = await request.get(
      `${apiBaseUrl}/api/v1/dashboard/pending-signatures`,
      { headers },
    );
    const pendingAfter = (await pendingAfterResponse.json()).data.totalPending;
    expect(pendingAfter).toBe(pendingBefore - 1);
  });

  test('@report53 ALT-07 khoá ký không hợp lệ không làm đổi trạng thái hợp đồng', async ({ request }) => {
    requireDestructive();
    const env = requireEnv([
      'E2E_DIRECTOR_EMAIL',
      'E2E_DIRECTOR_PASSWORD',
      'E2E_INVALID_SIGN_CONTRACT_ID',
      'E2E_INVALID_SIGN_PAYLOAD',
    ]);
    const token = await loginApi(request, env.E2E_DIRECTOR_EMAIL, env.E2E_DIRECTOR_PASSWORD);
    const headers = authHeaders(token);
    const contractUrl = `${apiBaseUrl}/api/v1/contracts/${env.E2E_INVALID_SIGN_CONTRACT_ID}`;
    const statusBefore = (await (await request.get(contractUrl, { headers })).json())
      .data.contractStatus;

    let invalidPayload;
    try {
      invalidPayload = JSON.parse(env.E2E_INVALID_SIGN_PAYLOAD);
    } catch {
      throw new Error('E2E_INVALID_SIGN_PAYLOAD phải là JSON hợp lệ.');
    }
    const signResponse = await request.post(`${contractUrl}/sign/complete`, {
      headers,
      data: invalidPayload,
    });
    expect(signResponse.status()).toBeGreaterThanOrEqual(400);

    const statusAfter = (await (await request.get(contractUrl, { headers })).json())
      .data.contractStatus;
    expect(statusAfter).toBe(statusBefore);
  });

  test('@report53 ALT-08 OTP chỉ dùng được một lần', async ({ request }) => {
    requireDestructive();
    const env = requireEnv([
      'E2E_RESET_EMAIL',
      'E2E_RESET_OTP',
      'E2E_RESET_NEW_PASSWORD',
    ]);
    const payload = {
      email: env.E2E_RESET_EMAIL,
      otp: env.E2E_RESET_OTP,
      newPassword: env.E2E_RESET_NEW_PASSWORD,
      newPasswordConfirm: env.E2E_RESET_NEW_PASSWORD,
    };

    const firstReset = await request.post(`${apiBaseUrl}/api/v1/auth/reset-password`, {
      data: payload,
    });
    expect(firstReset.status(), await firstReset.text()).toBe(201);

    const secondReset = await request.post(`${apiBaseUrl}/api/v1/auth/reset-password`, {
      data: payload,
    });
    expect(secondReset.status()).toBe(400);
    expect(JSON.stringify(await responseBody(secondReset))).toMatch(/OTP.*empty|OTP.*hiệu lực/i);
  });

  test('@report53 ALT-09 dashboard không cộng Cancelled vào Active hoặc Pending', async ({ request }) => {
    const env = requireEnv(['E2E_ADMIN_EMAIL', 'E2E_ADMIN_PASSWORD']);
    const token = await loginApi(request, env.E2E_ADMIN_EMAIL, env.E2E_ADMIN_PASSWORD);
    const headers = authHeaders(token);

    const statsResponse = await request.get(
      `${apiBaseUrl}/api/v1/dashboard/statistical-reports`,
      { headers },
    );
    expect(statsResponse.ok(), await statsResponse.text()).toBeTruthy();
    const stats = (await statsResponse.json()).data;
    expect(stats.activeAgreements).toBe(statusValue(stats.statusDistribution, 'ACTIVE'));
    expect(stats.canceledAgreements).toBe(statusValue(stats.statusDistribution, 'CANCELLED'));

    const pendingResponse = await request.get(
      `${apiBaseUrl}/api/v1/dashboard/pending-signatures`,
      { headers },
    );
    const pending = (await pendingResponse.json()).data;
    const expectedPending = ['PENDING_SIGNATURE', 'PENDING_DIRECTOR_SIGNATURE', 'PENDING_PARTNER_SIGNATURE']
      .reduce((sum, status) => sum + statusValue(stats.statusDistribution, status), 0);
    expect(pending.totalPending).toBe(expectedPending);
  });

  test('@report53 ALT-10 hợp đồng chưa hết hạn vẫn Active sau một nhịp scheduler', async ({ request }) => {
    const env = requireEnv([
      'E2E_ADMIN_EMAIL',
      'E2E_ADMIN_PASSWORD',
      'E2E_FUTURE_ACTIVE_CONTRACT_ID',
    ]);
    const token = await loginApi(request, env.E2E_ADMIN_EMAIL, env.E2E_ADMIN_PASSWORD);
    const headers = authHeaders(token);
    const url = `${apiBaseUrl}/api/v1/contracts/${env.E2E_FUTURE_ACTIVE_CONTRACT_ID}`;

    const first = (await (await request.get(url, { headers })).json()).data;
    expect(first.contractStatus).toBe('ACTIVE');
    expect(new Date(first.expirationDate).getTime()).toBeGreaterThan(Date.now());

    await new Promise((resolve) => setTimeout(resolve, 2_000));
    const second = (await (await request.get(url, { headers })).json()).data;
    expect(second.contractStatus).toBe('ACTIVE');
  });
});


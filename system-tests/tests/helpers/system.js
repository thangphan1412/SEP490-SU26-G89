import { expect, test } from '@playwright/test';

export const apiBaseUrl = (process.env.E2E_API_URL || 'http://localhost:8080')
  .replace(/\/$/, '');

export function requireEnv(names) {
  const missing = names.filter((name) => !process.env[name]);
  test.skip(missing.length > 0, `Thiếu biến môi trường: ${missing.join(', ')}`);
  return Object.fromEntries(names.map((name) => [name, process.env[name]]));
}

export function requireDestructive() {
  test.skip(
    process.env.E2E_ALLOW_DESTRUCTIVE !== 'true',
    'Đặt E2E_ALLOW_DESTRUCTIVE=true trên database test riêng để chạy ca thay đổi dữ liệu.',
  );
}

export async function loginUi(page, email, password) {
  await page.goto('/login');
  await page.getByPlaceholder('john.doe@company.com').fill(email);
  await page.locator('input[type="password"]').fill(password);

  const loginResponse = page.waitForResponse((response) =>
    response.url().endsWith('/api/v1/auth/login')
      && response.request().method() === 'POST');
  await page.getByRole('button', { name: /sign in/i }).click();
  const response = await loginResponse;

  expect(response.ok(), await response.text()).toBeTruthy();
  await expect.poll(() => page.evaluate(() => localStorage.getItem('token')))
    .not.toBeNull();
}

export async function loginApi(request, email, password) {
  const response = await request.post(`${apiBaseUrl}/api/v1/auth/login`, {
    data: { email, password },
  });
  expect(response.ok(), await response.text()).toBeTruthy();
  const body = await response.json();
  expect(body.data?.token).toEqual(expect.any(String));
  return body.data.token;
}

export function authHeaders(token) {
  return { Authorization: `Bearer ${token}` };
}

export async function responseBody(response) {
  const text = await response.text();
  try {
    return JSON.parse(text);
  } catch {
    return { raw: text };
  }
}

export function statusValue(distribution, status) {
  return (distribution || [])
    .filter((item) => item.label === status)
    .reduce((total, item) => total + Number(item.value || 0), 0);
}

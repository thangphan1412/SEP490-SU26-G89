import { expect, test } from '@playwright/test';
import { loginUi, requireEnv } from './helpers/system.js';

test.describe('Report 5.3 - browser system tests', () => {
  test('@smoke Chưa đăng nhập bị chuyển về /login', async ({ page }) => {
    await page.goto('/home_page');
    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('heading', { name: 'Sign in to your account' }))
      .toBeVisible();
  });

  test('@smoke Sai mật khẩu bị API từ chối và không tạo phiên đăng nhập', async ({ page }) => {
    await page.goto('/login');
    await page.getByPlaceholder('john.doe@company.com')
      .fill('not-a-system-user@example.com');
    await page.locator('input[type="password"]').fill('WrongPass1!');

    const loginResponse = page.waitForResponse((response) =>
      response.url().endsWith('/api/v1/auth/login')
        && response.request().method() === 'POST');
    await page.getByRole('button', { name: /sign in/i }).click();
    const response = await loginResponse;

    expect(response.status()).toBe(400);
    await expect(page).toHaveURL(/\/login$/);
    expect(await page.evaluate(() => localStorage.getItem('token'))).toBeNull();
  });

  test('@report53 E2E-01 đăng nhập và mở được các màn hình của luồng 31 bước', async ({ page }) => {
    const env = requireEnv(['E2E_ADMIN_EMAIL', 'E2E_ADMIN_PASSWORD']);
    await loginUi(page, env.E2E_ADMIN_EMAIL, env.E2E_ADMIN_PASSWORD);

    const workflowRoutes = [
      ['/department-management/list', 'Departments'],
      ['/role-management/list', 'Roles'],
      ['/user-management/list', 'Users'],
      ['/project-management/list', 'Projects'],
      ['/permission/list', 'Permissions'],
      ['/contract-types', 'Contract Types'],
      ['/contract-templates', 'Contract Templates'],
      ['/contract-management/list', 'Contract'],
      ['/dashboard/contract-statistical-reports', 'Contract'],
    ];

    for (const [route, headingPattern] of workflowRoutes) {
      await test.step(`Mở ${route}`, async () => {
        await page.goto(route);
        await expect(page).not.toHaveURL(/\/login$/);
        await expect(page.getByText(new RegExp(headingPattern, 'i')).first())
          .toBeVisible();
      });
    }
  });

  test('@report53 ALT-01 nhân viên không có quyền không mở được chi tiết dự án', async ({ page }) => {
    const env = requireEnv([
      'E2E_EMPLOYEE_EMAIL',
      'E2E_EMPLOYEE_PASSWORD',
      'E2E_FORBIDDEN_PROJECT_ID',
    ]);
    await loginUi(page, env.E2E_EMPLOYEE_EMAIL, env.E2E_EMPLOYEE_PASSWORD);

    const projectResponse = page.waitForResponse((response) =>
      response.url().endsWith(`/api/projects/${env.E2E_FORBIDDEN_PROJECT_ID}`));
    await page.goto(`/project-management/view?id=${env.E2E_FORBIDDEN_PROJECT_ID}`);
    const response = await projectResponse;

    expect(response.status()).toBeGreaterThanOrEqual(400);
    expect(response.status()).toBeLessThan(500);
    await expect(page.getByRole('heading', { name: 'Unable to open project' }))
      .toBeVisible();
  });
});

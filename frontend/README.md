# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is enabled on this template. See [this documentation](https://react.dev/learn/react-compiler) for more information.

Note: This will impact Vite dev & build performances.

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
## Ae chú ý cấu trúc dự án
1 assets :
-file anh nao public thi vat het vao day
- trang nao can nhieu anh rieng thi tao folder rieng cho no
2. componets:
- common : cac loai nhu kieu button,...
- layout: kieu nhu footer, header noi chung la layout tung cai thi vat vao day
- neu y kien kieu tao folder thi bao ae ngoi lai xem co hop ly ko
3. util
- may cai kieu form date hay validate
4. pages
- cac man chinh kieu login hay gi ghep tu thang componet va them cac kieu no cho
5. service
- la may thang goi api be sang
  6.config
- noi cau hinh, cau hinh gi thi bao ae 1 tieng cho dong bo

luu y : tat ca may thang tren deu chi la file chung, kieu manage thi ben trong no co nhieu man cu hop ly thi tao them folder cho no
- 
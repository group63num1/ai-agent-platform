# vue3-001

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Compile and Minify for Production

```sh
npm run build
```

### 后端集成

当前仓库已移除本地 `mock-server` 的强制使用；前端现在直接请求真实后端（路径 `/api/*`）。

开发运行：

```sh
npm install
npm run dev
```

如果需要本地模拟测试与历史实现保持兼容，请参阅 `AGENTS_API.md` 中的接口说明来自行搭建或与后端对接。

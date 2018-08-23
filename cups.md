# CUPS简介

通用UNIX打印系统（Common Unix Print System，简称CUPS），是一种模块化开源打印系统。

通过以下各项可管理在 Oracle Solaris 操作系统 (operating system, OS) 中使用 CUPS 进行打印 ：

1. CUPS 命令行实用程序－这些命令包括新的 CUPS 打印命令以及之前由 LP 打印服务使用的一些打印命令。 
2. CUPS Web 浏览器界面－转至 `http://localhost:631`。 
3. CUPS 打印管理器 GUI－您可以从包括 GNOME 2.30 的 Oracle Solaris Desktop 或通过在终端窗口中键入 `system-config-printer` 命令访问此 GUI。 

# CUPS常用命令

| 命令          | 任务                                     |
| ------------- | ---------------------------------------- |
| `cancel`      | 取消打印请求                             |
| `cuspaccept`  | 允许将打印请求排队到指定的目标           |
| `cuspdisable` | 禁用指定的打印机或类                     |
| `cupsenable`  | 启用指定的打印机或类                     |
| `cupsreject`  | 拒绝将打印请求排队到指定的目标           |
| `lp`          | 提交打印请求                             |
| `lpadmin`     | 设置或更改打印机或类的配置               |
| `lpc`         | 提供对 CUPS 打印队列和类队列的有限控制   |
| `lpinfo`      | 显示 CUPS 服务器已知的可用设备或驱动程序 |
| `lpmove`      | 将指定作业或所有作业移至新的目标中       |
| `lpoptions`   | 显示或设置打印机选项和缺省值             |
| `lpq`         | 显示当前打印队列状态                     |
| `lpr`         | 提交打印请求                             |
| `lprm`        | 取消已排队等候打印的打印作业             |
| `lpstat`      | 显示队列和请求的状态信息                 |

[cups官方文档](https://docs.oracle.com/cd/E26926_01/html/E25812/gllgm.html#scrolltoc)


# Tech Guns Community Edition Addon

## 简介

这是由异天庄团队开发的科技枪社区版的拓展mod，这个mod是为整合包《征途，守望·大远征》量身定做的，加入了新的武器，护甲，敌人，机器，材料等。

！！！注意！！！
本项目使用了AI工具进行维护和开发，谨慎参考

## 功能特性

### 新武器：

加入了灵感来源于《战锤40k》的一些武器，如链锯剑，动力锤，爆弹枪等。

## 安装方法

1. 确保已安装Minecraft Forge 1.12.2
2. 安装科技枪社区版（Tech Guns CE）2.1.3或更高版本
3. 将本Mod的jar文件放入游戏的mods文件夹
4. 启动游戏

## 兼容性

本Mod的兼容性可参考科技枪社区版，暂无确定的不兼容信息，有问题可以在issue上报告。

<br />

## 多人游戏支持

本Mod完全支持多人游戏，所有武器和物品都可以在服务器上正常使用。

## 开发信息

- **版本**: 0.30.2（真值在 `build.gradle` 的 `version` 与 `TGCEAddon.VERSION`，两处必须一致）
- **Minecraft版本**: 1.12.2
- **Forge版本**: 14.23.5.2860（ForgeGradle 3 / Gradle 4.9 / MCP snapshot 20171003-1.12 / Java 8）
- **前置**: 科技枪社区版 2.2.0.1（`libs/techguns-2.2.0.1.jar`，compileOnly）
- **开发团队**: TeamYTZ

### 版本号规则

`0.<大更新>.<小更新或修复>`：功能成体系地增加时进位中间位，修 bug / 调表现只加末位。
**每次改动都要升版本号**，并同步 `build.gradle` 与 `TGCEAddon.VERSION` 两处。

### 构建与回档

```powershell
# 构建（本机无系统 JDK，gradlew.bat 会自动用工作区 .tools/ 里的便携 JDK 8）
cd TeamytzTGCEAddonP-main\TeamytzTGCEAddon
.\gradlew.bat build          # 产物 build\libs\tgceaddon-<版本>.jar（已 reobf，旧版本不删）

# 回档（仓库根 = 工作区根 S:\mc\mod\TGCEAddon）
git log --oneline            # 看每次更新的版本与说明
git show <提交>              # 看某次改了什么
git revert <提交>            # 撤销某次改动（保留历史，推荐）
git checkout <提交> -- <路径>  # 只把某个文件恢复到某个版本
```

每次 mod 更新完成后：升版本号 → `gradlew.bat build` → 同步到测试实例
`G:\MC\.minecraft\versions\tgaddontest\mods`（**删掉旧版本 jar，只留新的**）→ `git commit`。

## 许可证

本Mod遵循相关开源协议，详情请查看LICENSE文件。

## 联系方式

如有问题或建议，请通过GitHub Issues联系我们。

## 致谢

感谢科技枪团队创建了原版Mod，为本扩展提供了基础。

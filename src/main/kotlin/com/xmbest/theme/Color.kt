package com.xmbest.theme

import androidx.compose.ui.graphics.Color

/**
 * 主要颜色是应用中最常显示的色彩，用于关键组件和品牌标识，如应用栏、按钮、标签页
 * 次要颜色用于辅助主要颜色，增强界面的层次感和视觉吸引力。它通常用于次要按钮、浮动操作按钮、文本框等组件
 * “On”颜色是出现在其他元素上方的颜色，如文本、图标等。它们需要满足无障碍功能要求，并与所在表面的颜色有足够的对比度，以确保内容的可读性和可访问性
 * 背景色是应用界面的基础色彩，用于填充整个屏幕或特定区域，提供舒适的视觉环境
 * 表面色用于呈现抬升的表面或“纸张”，如卡片、对话框、菜单等。它通过不同的色调和阴影来模拟物理世界的深度和层次感
 */


/** 通用错误色*/
val error = Color(0xFFf4433c)

/** 蓝色主题*/
val blue_primary = Color(0xFF4382EC)
val blue_onPrimary = Color.White
val blue_background = Color(0xFFBBD6FD)
val blue_onBackground = Color(0xFF1b3041)
val blue_surface = Color(0xFFE2EEFF)
val blue_onSurface = Color(0xFF1B3041)


/** 经典浅色*/
val classic_primary = blue_primary
val classic_onPrimary = blue_onPrimary
val classic_second = Color.White
val classic_onSecond = Color.Black
val classic_background = Color(0xFFEFF5F9)
val classic_onBackground = Color(0xEE626465)


/** 深色主题 */
val night_primary = blue_primary
val night_onPrimary = Color(0xFFEBE5E0)
val night_second = Color(0xFF2C2C2D)
val night_onSecond = Color(0xCCEBE5E0)
val night_background = Color(0xFF202021)
val night_onBackground = Color(0x99EBE5E0)

/** 紫色主题*/
val purple_primary = Color(0xFF9563b5)
val purple_onPrimary = Color.White
val purple_background = Color(0xFFDAC2E8)
val purple_onBackground = Color(0xFF2D1B3E)
val purple_surface = Color(0xFFE9E2F3)
val purple_onSurface = Color(0xFF2D1B3E)

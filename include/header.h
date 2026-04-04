
#pragma once

// Core ImGui
#include "imgui/imgui.h"

// FreeType support
#include "imgui/misc/freetype/imgui_freetype.h"

#ifdef __cplusplus
extern "C" {
#endif

// Context
void imgui_create_context();
void imgui_destroy_context();

// Frame lifecycle
void imgui_new_frame();
void imgui_render();

// Basic widgets
void imgui_text(const char* text);
bool imgui_button(const char* label);

// FreeType font setup
void imgui_enable_freetype();

#ifdef __cplusplus
}
#endif

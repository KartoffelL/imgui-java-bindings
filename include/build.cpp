#include "header.h"

// Context
void imgui_create_context() {
    ImGui::CreateContext();
}

void imgui_destroy_context() {
    ImGui::DestroyContext();
}

// Frame lifecycle
void imgui_new_frame() {
    ImGui::NewFrame();
}

void imgui_render() {
    ImGui::Render();
}

// Widgets
void imgui_text(const char* text) {
    ImGui::Text("%s", text);
}

bool imgui_button(const char* label) {
    return ImGui::Button(label);
}

// FreeType
void imgui_enable_freetype() {
    ImGuiIO& io = ImGui::GetIO();
    ImFontAtlas* atlas = io.Fonts;

    ImGuiFreeType::BuildFontAtlas(atlas, 0);
}

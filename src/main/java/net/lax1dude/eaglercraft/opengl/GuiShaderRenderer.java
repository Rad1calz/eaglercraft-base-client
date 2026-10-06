package net.lax1dude.eaglercraft.opengl;

import static net.lax1dude.eaglercraft.internal.PlatformOpenGL.*;
import static net.lax1dude.eaglercraft.opengl.RealOpenGLEnums.*;

import net.lax1dude.eaglercraft.internal.IProgramGL;
import net.lax1dude.eaglercraft.internal.IShaderGL;
import net.lax1dude.eaglercraft.internal.IUniformGL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.lax1dude.eaglercraft.opengl.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiShaderRenderer {

    private static final String VERTEX_SHADER = "EAGLER_VSH_LAYOUT_BEGIN()\nEAGLER_IN(0, vec2, a_position2f)\nEAGLER_VSH_LAYOUT_END()\nEAGLER_OUT(vec2, v_local)\nuniform vec4 u_rect; uniform vec2 u_screenSize; void main() { v_local = (a_position2f + 1.0) * 0.5; vec2 p = u_rect.xy + v_local * u_rect.zw; EAGLER_VERT_POSITION = vec4(p.x / u_screenSize.x * 2.0 - 1.0, 1.0 - p.y / u_screenSize.y * 2.0, 0.0, 1.0); }";
    private static final String FRAGMENT_SHADER = "EAGLER_IN(vec2, v_local)\nEAGLER_FRAG_OUT()\nuniform vec4 u_baseColor; uniform vec4 u_accentColor; uniform vec2 u_size; uniform float u_radius; uniform float u_time; float roundedBox(vec2 p, vec2 halfSize, float radius) { vec2 q = abs(p) - halfSize + vec2(radius); return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius; } void main() { vec2 halfSize = u_size * 0.5; vec2 p = (v_local - 0.5) * u_size; float d = roundedBox(p, halfSize, u_radius); float mask = 1.0 - smoothstep(-1.0, 1.0, d); float edge = 1.0 - smoothstep(0.0, 1.5, abs(d + 0.75)); float pulse = 0.5 + 0.5 * sin(u_time * 2.0 + v_local.x * 3.0); float sweep = smoothstep(0.0, 0.12, v_local.y) * (1.0 - smoothstep(0.12, 0.2, v_local.y)); vec3 color = mix(u_baseColor.rgb, u_accentColor.rgb, 0.10 + sweep * (0.12 + pulse * 0.08)); color += u_accentColor.rgb * edge * 0.7; EAGLER_FRAG_COLOR = vec4(color, u_baseColor.a * mask); }";

    private static IProgramGL program;
    private static IUniformGL rectUniform;
    private static IUniformGL screenSizeUniform;
    private static IUniformGL sizeUniform;
    private static IUniformGL baseColorUniform;
    private static IUniformGL accentColorUniform;
    private static IUniformGL radiusUniform;
    private static IUniformGL timeUniform;

    public static boolean isEnabled() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft != null && minecraft.gameSettings != null && minecraft.gameSettings.customUiEnabled;
    }

    private GuiShaderRenderer() {
    }

    private static void initialize() {
        if (program != null) {
            return;
        }

        DrawUtils.init();
        IShaderGL vertex = _wglCreateShader(GL_VERTEX_SHADER);
        IShaderGL fragment = _wglCreateShader(GL_FRAGMENT_SHADER);
        _wglShaderSource(vertex, GLSLHeader.getVertexHeaderCompat(VERTEX_SHADER, "precision highp float;\n"));
        _wglCompileShader(vertex);
        _wglShaderSource(fragment, GLSLHeader.getFragmentHeaderCompat(FRAGMENT_SHADER, "precision highp float;\n"));
        _wglCompileShader(fragment);
        if (_wglGetShaderi(vertex, GL_COMPILE_STATUS) != GL_TRUE || _wglGetShaderi(fragment, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new IllegalStateException("Failed to compile the client GUI shaders");
        }

        program = _wglCreateProgram();
        _wglAttachShader(program, vertex);
        _wglAttachShader(program, fragment);
        if (EaglercraftGPU.checkOpenGLESVersion() == 200) {
            VSHInputLayoutParser.applyLayout(program, VSHInputLayoutParser.getShaderInputs(VERTEX_SHADER));
        }
        _wglLinkProgram(program);
        _wglDetachShader(program, vertex);
        _wglDetachShader(program, fragment);
        _wglDeleteShader(vertex);
        _wglDeleteShader(fragment);
        if (_wglGetProgrami(program, GL_LINK_STATUS) != GL_TRUE) {
            program = null;
            throw new IllegalStateException("Failed to link the client GUI shader program");
        }

        rectUniform = _wglGetUniformLocation(program, "u_rect");
        screenSizeUniform = _wglGetUniformLocation(program, "u_screenSize");
        sizeUniform = _wglGetUniformLocation(program, "u_size");
        baseColorUniform = _wglGetUniformLocation(program, "u_baseColor");
        accentColorUniform = _wglGetUniformLocation(program, "u_accentColor");
        radiusUniform = _wglGetUniformLocation(program, "u_radius");
        timeUniform = _wglGetUniformLocation(program, "u_time");
    }

    public static void drawPanel(int x, int y, int width, int height, int screenWidth, int screenHeight,
            int baseColor, int accentColor, float radius, float time) {
        if (width <= 0 || height <= 0 || screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        boolean restoreDepthTest = GlStateManager.stateDepthTest;
        if (restoreDepthTest) {
            GlStateManager.disableDepth();
        }
        if (!isEnabled()) {
            drawRoundedUnderlay(x, y, width, height, baseColor, radius);
            if (restoreDepthTest) {
                GlStateManager.enableDepth();
            }
            return;
        }
        drawRoundedUnderlay(x, y, width, height, baseColor, radius);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        initialize();
        EaglercraftGPU.bindGLShaderProgram(program);
        EaglercraftGPU.bindGLVertexArray(DrawUtils.standardQuad2DVAO);
        _wglUniform4f(rectUniform, x, y, width, height);
        _wglUniform2f(screenSizeUniform, screenWidth, screenHeight);
        _wglUniform2f(sizeUniform, width, height);
        setColor(baseColorUniform, baseColor);
        setColor(accentColorUniform, accentColor);
        _wglUniform1f(radiusUniform, radius);
        _wglUniform1f(timeUniform, time);
        DrawUtils.drawStandardQuad2D();
        EaglercraftGPU.bindGLVertexArray(null);
        EaglercraftGPU.bindGLShaderProgram(null);
        GlStateManager.disableBlend();
        if (restoreDepthTest) {
            GlStateManager.enableDepth();
        }
    }

    private static void drawRoundedUnderlay(int x, int y, int width, int height, int color, float radius) {
        int corner = Math.min(Math.max(0, Math.round(radius)), Math.min(width, height) / 2);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alpha = (color >>> 24) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertices = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        vertices.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        addRectangle(vertices, x + corner, y, x + width - corner, y + height, red, green, blue, alpha);
        if (corner > 0) {
            addRectangle(vertices, x, y + corner, x + corner, y + height - corner, red, green, blue, alpha);
            addRectangle(vertices, x + width - corner, y + corner, x + width, y + height - corner,
                    red, green, blue, alpha);
            addCorner(vertices, x + corner, y + corner, corner, (float) Math.PI, (float) (Math.PI * 1.5),
                    red, green, blue, alpha);
            addCorner(vertices, x + width - corner, y + corner, corner, (float) (Math.PI * 1.5),
                    (float) (Math.PI * 2.0), red, green, blue, alpha);
            addCorner(vertices, x + width - corner, y + height - corner, corner, 0.0F,
                    (float) (Math.PI * 0.5), red, green, blue, alpha);
            addCorner(vertices, x + corner, y + height - corner, corner, (float) (Math.PI * 0.5),
                    (float) Math.PI, red, green, blue, alpha);
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void addRectangle(WorldRenderer vertices, float left, float top, float right, float bottom,
            float red, float green, float blue, float alpha) {
        addTriangle(vertices, left, top, right, top, right, bottom, red, green, blue, alpha);
        addTriangle(vertices, left, top, right, bottom, left, bottom, red, green, blue, alpha);
    }

    private static void addCorner(WorldRenderer vertices, float centerX, float centerY, float radius,
            float startAngle, float endAngle, float red, float green, float blue, float alpha) {
        int segments = Math.max(2, Math.min(8, (int) Math.ceil(radius / 2.0F)));
        float previousX = centerX + (float) Math.cos(startAngle) * radius;
        float previousY = centerY + (float) Math.sin(startAngle) * radius;
        for (int segment = 1; segment <= segments; ++segment) {
            float angle = startAngle + (endAngle - startAngle) * segment / segments;
            float nextX = centerX + (float) Math.cos(angle) * radius;
            float nextY = centerY + (float) Math.sin(angle) * radius;
            addTriangle(vertices, centerX, centerY, previousX, previousY, nextX, nextY,
                    red, green, blue, alpha);
            previousX = nextX;
            previousY = nextY;
        }
    }

    private static void addTriangle(WorldRenderer vertices, float x1, float y1, float x2, float y2, float x3,
            float y3, float red, float green, float blue, float alpha) {
        vertices.pos(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
        vertices.pos(x2, y2, 0.0D).color(red, green, blue, alpha).endVertex();
        vertices.pos(x3, y3, 0.0D).color(red, green, blue, alpha).endVertex();
    }

    private static void setColor(IUniformGL uniform, int color) {
        _wglUniform4f(uniform, (color >> 16 & 255) / 255.0f, (color >> 8 & 255) / 255.0f,
                (color & 255) / 255.0f, (color >>> 24) / 255.0f);
    }
}

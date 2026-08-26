package com.teamytz.tgceaddon.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Blockbench 导出的 JSON 模型渲染器
 * 支持：元素(from/to)、元素旋转(rotation)、每面独立 UV
 * 模型坐标：Blockbench 16网格像素坐标，1单位=1/16格
 * 渲染时以模型中心为原点（调用方负责摆放位置），模型中心取 X/Z 的中点、Y 底部
 */
public class BlockbenchJsonModel {

    private static final Gson GSON = new Gson();

    public static class Face {
        public float[] uv = new float[4]; // u1,v1,u2,v2 (16网格)
        public boolean cullface = false;
    }

    public static class Element {
        public float[] from = new float[3];
        public float[] to = new float[3];
        public float angle = 0;
        public String axis = "y";
        public float[] origin = new float[3];
        public Map<String, Face> faces = new HashMap<>();
    }

    private final List<Element> elements = new ArrayList<>();

    public BlockbenchJsonModel(ResourceLocation jsonLocation) {
        try {
            InputStream in = Minecraft.getMinecraft().getResourceManager()
                    .getResource(jsonLocation).getInputStream();
            JsonObject root = GSON.fromJson(new InputStreamReader(in), JsonObject.class);
            for (JsonElement e : root.getAsJsonArray("elements")) {
                JsonObject obj = e.getAsJsonObject();
                Element el = new Element();
                el.from = jsonArr(obj.getAsJsonArray("from"));
                el.to = jsonArr(obj.getAsJsonArray("to"));
                if (obj.has("rotation")) {
                    JsonObject rot = obj.getAsJsonObject("rotation");
                    el.angle = rot.get("angle").getAsFloat();
                    el.axis = rot.get("axis").getAsString();
                    el.origin = jsonArr(rot.getAsJsonArray("origin"));
                }
                JsonObject faces = obj.getAsJsonObject("faces");
                for (Map.Entry<String, JsonElement> fe : faces.entrySet()) {
                    JsonObject fo = fe.getValue().getAsJsonObject();
                    Face face = new Face();
                    JsonArray uvArr = fo.getAsJsonArray("uv");
                    face.uv[0] = uvArr.get(0).getAsFloat();
                    face.uv[1] = uvArr.get(1).getAsFloat();
                    face.uv[2] = uvArr.get(2).getAsFloat();
                    face.uv[3] = uvArr.get(3).getAsFloat();
                    if (fo.has("cullface")) {
                        face.cullface = !fo.get("cullface").getAsString().equals("none");
                    }
                    el.faces.put(fe.getKey(), face);
                }
                elements.add(el);
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Blockbench model " + jsonLocation, e);
        }
    }

    private static float[] jsonArr(JsonArray arr) {
        return new float[]{arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat()};
    }

    /**
     * 渲染整个模型（需先绑定纹理）
     * 模型坐标转换：X/Z 以 (0.5,0.5) 格为中心，Y 以方块底部为基准
     */
    public void render() {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        for (Element el : elements) {
            GlStateManager.pushMatrix();
            applyRotation(el);
            drawElement(el);
            GlStateManager.popMatrix();
        }

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private void applyRotation(Element el) {
        if (el.angle == 0) {
            return;
        }
        float ox = el.origin[0] / 16f - 0.5f;
        float oy = el.origin[1] / 16f;
        float oz = el.origin[2] / 16f - 0.5f;
        GlStateManager.translate(ox, oy, oz);
        if ("x".equals(el.axis)) {
            GlStateManager.rotate(el.angle, 1, 0, 0);
        } else if ("y".equals(el.axis)) {
            GlStateManager.rotate(el.angle, 0, 1, 0);
        } else {
            GlStateManager.rotate(el.angle, 0, 0, 1);
        }
        GlStateManager.translate(-ox, -oy, -oz);
    }

    private void drawElement(Element el) {
        float fx = el.from[0] / 16f - 0.5f;
        float fy = el.from[1] / 16f;
        float fz = el.from[2] / 16f - 0.5f;
        float tx = el.to[0] / 16f - 0.5f;
        float ty = el.to[1] / 16f;
        float tz = el.to[2] / 16f - 0.5f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        drawFace(buf, "north", el, fx, fy, fz, tx, ty, tz);
        drawFace(buf, "south", el, fx, fy, fz, tx, ty, tz);
        drawFace(buf, "east", el, fx, fy, fz, tx, ty, tz);
        drawFace(buf, "west", el, fx, fy, fz, tx, ty, tz);
        drawFace(buf, "up", el, fx, fy, fz, tx, ty, tz);
        drawFace(buf, "down", el, fx, fy, fz, tx, ty, tz);

        tess.draw();
    }

    private void drawFace(BufferBuilder buf, String faceName, Element el,
                          float fx, float fy, float fz, float tx, float ty, float tz) {
        Face face = el.faces.get(faceName);
        if (face == null) {
            return;
        }
        float u1 = face.uv[0] / 16f;
        float v1 = face.uv[1] / 16f;
        float u2 = face.uv[2] / 16f;
        float v2 = face.uv[3] / 16f;

        switch (faceName) {
            case "north": // z = fz
                buf.pos(fx, fy, fz).tex(u1, v1).endVertex();
                buf.pos(tx, fy, fz).tex(u2, v1).endVertex();
                buf.pos(tx, ty, fz).tex(u2, v2).endVertex();
                buf.pos(fx, ty, fz).tex(u1, v2).endVertex();
                break;
            case "south": // z = tz
                buf.pos(tx, fy, tz).tex(u1, v1).endVertex();
                buf.pos(fx, fy, tz).tex(u2, v1).endVertex();
                buf.pos(fx, ty, tz).tex(u2, v2).endVertex();
                buf.pos(tx, ty, tz).tex(u1, v2).endVertex();
                break;
            case "east": // x = tx
                buf.pos(tx, fy, fz).tex(u1, v1).endVertex();
                buf.pos(tx, fy, tz).tex(u2, v1).endVertex();
                buf.pos(tx, ty, tz).tex(u2, v2).endVertex();
                buf.pos(tx, ty, fz).tex(u1, v2).endVertex();
                break;
            case "west": // x = fx
                buf.pos(fx, fy, tz).tex(u1, v1).endVertex();
                buf.pos(fx, fy, fz).tex(u2, v1).endVertex();
                buf.pos(fx, ty, fz).tex(u2, v2).endVertex();
                buf.pos(fx, ty, tz).tex(u1, v2).endVertex();
                break;
            case "up": // y = ty
                buf.pos(fx, ty, fz).tex(u1, v1).endVertex();
                buf.pos(tx, ty, fz).tex(u2, v1).endVertex();
                buf.pos(tx, ty, tz).tex(u2, v2).endVertex();
                buf.pos(fx, ty, tz).tex(u1, v2).endVertex();
                break;
            case "down": // y = fy
                buf.pos(fx, fy, tz).tex(u1, v1).endVertex();
                buf.pos(tx, fy, tz).tex(u2, v1).endVertex();
                buf.pos(tx, fy, fz).tex(u2, v2).endVertex();
                buf.pos(fx, fy, fz).tex(u1, v2).endVertex();
                break;
        }
    }
}

package kameib.localizator.client.jei.fishingmadebetter;

import com.sun.istack.internal.NotNull;
import kameib.localizator.data.Drawing;
import kameib.localizator.data.Texture;
import kameib.localizator.data.fishingmadebetter.FishRequirementData;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.gui.elements.DrawableResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.item.ItemManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FishRequirementsRecipeWrapper implements ICraftingRecipeWrapper {
    private final List<List<ItemStack>> inputs = new ArrayList<>();
    private final ItemStack output;

    public List<BiomeDimensionOverlay> overlayList = new ArrayList<>();
    private final FishRequirementData fishRequirementData;
    
    public FishRequirementsRecipeWrapper(FishData fishData) {
        // INPUTS
        // ---------------  Fishing Rods  ------------------
        List<ItemStack> fishingRodStackList = new ArrayList<>();
        fishingRodStackList.add(new ItemStack(ItemManager.FISHING_ROD_WOOD));
        fishingRodStackList.add(new ItemStack(ItemManager.FISHING_ROD_IRON));
        fishingRodStackList.add(new ItemStack(ItemManager.FISHING_ROD_DIAMOND));        
        // ---------------      Reels     ------------------
        List<ItemStack> reelStackList = new ArrayList<>();
        if (fishData.minDeepLevel < REEL_BASIC_RANGE) {
            reelStackList.add(new ItemStack(ItemManager.REEL_BASIC));
        }
        if (fishData.minDeepLevel < REEL_FAST_RANGE) {
            reelStackList.add(new ItemStack(ItemManager.REEL_FAST));
        }
        if (fishData.minDeepLevel < REEL_LONG_RANGE) {
            reelStackList.add(new ItemStack(ItemManager.REEL_LONG));
        }
        // ---------------     Bobbers    ------------------
        List<ItemStack> bobberStackList = new ArrayList<>();
        if (fishData.liquid == FishData.FishingLiquid.LAVA) {
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_OBSIDIAN));
        } else if (fishData.liquid == FishData.FishingLiquid.VOID) {
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_VOID));
        } else if (fishData.liquid == FishData.FishingLiquid.ANY) {
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_BASIC));
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_HEAVY));
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_LIGHTWEIGHT));
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_OBSIDIAN));
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_VOID));
        } else {
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_BASIC));
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_HEAVY));
            bobberStackList.add(new ItemStack(ItemManager.BOBBER_LIGHTWEIGHT));
        }

        inputs.add(fishingRodStackList);
        inputs.add(reelStackList);
        inputs.add(bobberStackList);
        inputs.add(Collections.singletonList(new ItemStack(Item.getByNameOrId("fishingmadebetter:bait_bucket"))));
        // ---------------      Baits     ------------------
        Item bait;
        for (String baitName : fishData.baitItemMap.keySet()) {
            for (int baitMetadata : fishData.baitItemMap.get(baitName)) {
                bait = Item.getByNameOrId(baitName);
                if (bait != null) {
                    this.inputs.add(Collections.singletonList(new ItemStack(bait, 1, baitMetadata)));
                }
            }
        }
        // OUTPUT
        this.output = new ItemStack(Item.getByNameOrId(fishData.itemId), 1, fishData.itemMetaData);
        
        // Initialize Y Meter & Minigame Overlays
        fishRequirementData = new FishRequirementData(fishData);
        for (Integer dimension : fishRequirementData.dimensionList) {
            if (dimension == -1) {
                overlayList.add(new BiomeDimensionOverlay(fishRequirementData, dimension, null));
            } else if (dimension == 1) {
                overlayList.add(new BiomeDimensionOverlay(fishRequirementData, dimension, null));
            } else {
                for (String biome : fishRequirementData.biomeTagList) {
                    overlayList.add(new BiomeDimensionOverlay(fishRequirementData, dimension, biome));
                }
            }
        }
    }
    
    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, this.inputs);
        ingredients.setOutput(VanillaTypes.ITEM, this.output);
    }

    @Override
    @Nonnull
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();
        if ((mouseX >= YMETER_X_START && mouseX < YMETER_X_START + DRAWING_YMETER_BACKGROUND.width)
                && (mouseY >= YMETER_Y_START && mouseY < YMETER_Y_START + DRAWING_YMETER_BACKGROUND.height)) {
            // Detailed Y range
            tooltip.add(TextFormatting.GRAY + "Y: " + TextFormatting.WHITE + fishRequirementData.minYLevel + TextFormatting.GRAY + " - " + TextFormatting.WHITE + fishRequirementData.maxYLevel);
            return tooltip;
        }

        if ((mouseX >= MINIGAME_X_START && mouseX < MINIGAME_X_START + DRAWING_OUTLINE.width)
                && (mouseY >= MINIGAME_Y_START && mouseY < MINIGAME_Y_START + DRAWING_OUTLINE.height)) {
            // Time to fish
            tooltip.add(TextFormatting.GRAY + I18n.format("jei.fishingmadebetter.category.fish_requirements.minigame.time.tooltip",
                    TextFormatting.WHITE + I18n.format("notif.fishingmadebetter.fish_tracker.creative.time." + fishRequirementData.timeToFish)));
            // Requires rain?
            tooltip.add(TextFormatting.GRAY + I18n.format("jei.fishingmadebetter.category.fish_requirements.minigame.rain.tooltip",
                    TextFormatting.WHITE + I18n.format("notif.fishingmadebetter.fish_tracker.creative.rain." + fishRequirementData.rainRequired)));
            // Requires thunderstorm?
            tooltip.add(TextFormatting.GRAY + I18n.format("jei.fishingmadebetter.category.fish_requirements.minigame.thunder.tooltip",
                    TextFormatting.WHITE + I18n.format("notif.fishingmadebetter.fish_tracker.creative.thunder." + fishRequirementData.thunderRequired)));
            return tooltip;
        }

        return Collections.emptyList();
    }

    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        beginRenderingTransparency();
        {
            // ---------- MINIGAME OVERLAY ----------
            int biomeDisplay = minecraft.player.ticksExisted % (overlayList.size() * 20);
            overlayList.get(biomeDisplay / 20).drawMiniGame(minecraft, MINIGAME_X_START, MINIGAME_Y_START);
            overlayList.get(biomeDisplay / 20).drawYmeter(minecraft, YMETER_X_START, YMETER_Y_START);
        }
        finishRenderingTransparency();
    }
    
    private void beginRenderingTransparency() {
        GlStateManager.pushMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
    }

    private void finishRenderingTransparency() {
        GlStateManager.popMatrix();
    }

    public static class BiomeDimensionOverlay {
        public DrawableResource[] minigameInner = new DrawableResource[5]; // Liquid, Sky, Surroundings, Rain, Thunder
        public DrawableResource minigameBorder;
        public DrawableResource[] yMeter = new DrawableResource[4]; // Background, Range, Foreground, OceanLevel 

        public BiomeDimensionOverlay(FishRequirementData fishRequirementData, int dimension, String biomeTag) {

            // Mini game border
            minigameBorder = new DrawableResource(TEXTURE_MINIGAME_OUTLINE.texture, DRAWING_OUTLINE.u, DRAWING_OUTLINE.v, DRAWING_OUTLINE.width, DRAWING_OUTLINE.height,
                    0, 0, 0, 0, TEXTURE_MINIGAME_OUTLINE.textureWidth, TEXTURE_MINIGAME_OUTLINE.textureHeight);

            // Y METER
            // Background
            yMeter[0] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_YMETER_BACKGROUND.u, DRAWING_YMETER_BACKGROUND.v, DRAWING_YMETER_BACKGROUND.width, DRAWING_YMETER_BACKGROUND.height,
                    0, 0, 0, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
            // Range
            // YMETER_Y_START + 1 + (MAX_Y_LEVEL - (m_maxYLevel / 5)), DRAWING_YRANGE.u, DRAWING_YRANGE.v + (MAX_Y_LEVEL - ((float) m_maxYLevel / 5)), DRAWING_YRANGE.width, m_maxYLevel / 5 - Math.max(m_minYLevel / 5, 0) + 1, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
            yMeter[1] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_YRANGE.u, DRAWING_YRANGE.v + (MAX_Y_LEVEL - (fishRequirementData.maxYLevel / 5)), DRAWING_YRANGE.width, fishRequirementData.maxYLevel / 5 - Math.max(fishRequirementData.minYLevel / 5, 0) + 1,
                    YMETER_RANGE_TOP_OFFSET + (MAX_Y_LEVEL - (fishRequirementData.maxYLevel / 5)), 0, YMETER_RANGE_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
            // Foreground
            yMeter[2] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_YMETER_FOREGROUND.u, DRAWING_YMETER_FOREGROUND.v, DRAWING_YMETER_FOREGROUND.width, DRAWING_YMETER_FOREGROUND.height,
                    0, 0, 0, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);

            switch (dimension) {
                case -1: // Nether
                {
                    // Liquid
                    minigameInner[0] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_LIQUID_LAVA.u, DRAWING_LIQUID_LAVA.v, DRAWING_LIQUID_LAVA.width, DRAWING_LIQUID_LAVA.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    // Sky
                    minigameInner[1] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    // Surroundings
                    minigameInner[2] = new DrawableResource(TEXTURE_DIMENSION_CAVE.texture, DRAWING_NETHER.u, DRAWING_NETHER.v, DRAWING_NETHER.width, DRAWING_NETHER.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_DIMENSION_CAVE.textureWidth, TEXTURE_DIMENSION_CAVE.textureHeight);
                    // Rain
                    minigameInner[3] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    // Thunder
                    minigameInner[4] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);

                    yMeter[3] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_LAVA_LEVEL.u, DRAWING_LAVA_LEVEL.v, DRAWING_LAVA_LEVEL.width, DRAWING_LAVA_LEVEL.height,
                            +1 + (MAX_Y_LEVEL - (YMETER_LAVA_OCEAN / 5)), 0, YMETER_LEVEL_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
                }
                    break;
                case 1: // The End
                {
                    // Liquid
                    minigameInner[0] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_LIQUID_VOID.u, DRAWING_LIQUID_VOID.v, DRAWING_LIQUID_VOID.width, DRAWING_LIQUID_VOID.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    // Sky
                    minigameInner[1] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    // Surroundings
                    minigameInner[2] = new DrawableResource(TEXTURE_DIMENSION_CAVE.texture, DRAWING_THE_END.u, DRAWING_THE_END.v, DRAWING_THE_END.width, DRAWING_THE_END.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_DIMENSION_CAVE.textureWidth, TEXTURE_DIMENSION_CAVE.textureHeight);
                    // Rain
                    minigameInner[3] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    // Thunder
                    minigameInner[4] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);

                    yMeter[3] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_VOID_LEVEL.u, DRAWING_VOID_LEVEL.v, DRAWING_VOID_LEVEL.width, DRAWING_VOID_LEVEL.height,
                            1 + (MAX_Y_LEVEL - (YMETER_VOID / 5)), 0, YMETER_LEVEL_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
                    }
                    break;
                default: // Overworld
                {
                    switch (fishRequirementData.liquid) {
                        case LAVA:
                            // Liquid
                            minigameInner[0] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_LIQUID_LAVA.u, DRAWING_LIQUID_LAVA.v, DRAWING_LIQUID_LAVA.width, DRAWING_LIQUID_LAVA.height,
                                    MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                            break;
                        case VOID:
                            // Liquid
                            minigameInner[0] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_LIQUID_VOID.u, DRAWING_LIQUID_VOID.v, DRAWING_LIQUID_VOID.width, DRAWING_LIQUID_VOID.height,
                                    MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                            break;
                        case ANY:
                            // Liquid
                            minigameInner[0] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_LIQUID_ANY.u, DRAWING_LIQUID_ANY.v, DRAWING_LIQUID_ANY.width, DRAWING_LIQUID_ANY.height,
                                    MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
                            break;
                        default:
                            // Liquid
                            minigameInner[0] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_BIOME_LIQUIDS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).u, DRAWING_BIOME_LIQUIDS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).v, DRAWING_BIOME_LIQUIDS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).width, DRAWING_BIOME_LIQUIDS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).height,
                                    MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    }
                    switch (fishRequirementData.timeToFish) {
                        case NIGHT:
                            // Sky
                            minigameInner[1] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_NIGHT.u, DRAWING_NIGHT.v, DRAWING_NIGHT.width, DRAWING_NIGHT.height,
                                    MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                            break;
                        case ANY:
                            // Sky
                            minigameInner[1] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_DAY_NIGHT.u, DRAWING_DAY_NIGHT.v, DRAWING_DAY_NIGHT.width, DRAWING_DAY_NIGHT.height,
                                    MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
                            break;
                        default:
                            // Sky
                            minigameInner[1] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_DAY.u, DRAWING_DAY.v, DRAWING_DAY.width, DRAWING_DAY.height,
                                    MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    }
                    // Surroundings
                    minigameInner[2] = new DrawableResource(TEXTURE_BIOME.texture, DRAWING_BIOME_SURROUNDINGS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).u, DRAWING_BIOME_SURROUNDINGS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).v, DRAWING_BIOME_SURROUNDINGS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).width, DRAWING_BIOME_SURROUNDINGS.get(FishRequirementData.texturePosFromBiomeTag(biomeTag)).height,
                            MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_BIOME.textureWidth, TEXTURE_BIOME.textureHeight);
                    if (fishRequirementData.rainRequired) {
                        // Rain
                        minigameInner[3] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_RAIN.u, DRAWING_RAIN.v, DRAWING_RAIN.width, DRAWING_RAIN.height,
                                MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
                    } else {
                        // Rain
                        minigameInner[3] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                                MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    }
                    if (fishRequirementData.thunderRequired) {
                        // Thunder
                        minigameInner[4] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_THUNDERSTORM.u, DRAWING_THUNDERSTORM.v, DRAWING_THUNDERSTORM.width, DRAWING_THUNDERSTORM.height,
                                MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
                    } else {
                        // Thunder
                        minigameInner[4] = new DrawableResource(TEXTURE_LIQUID_SKY.texture, DRAWING_TIMELESS.u, DRAWING_TIMELESS.v, DRAWING_TIMELESS.width, DRAWING_TIMELESS.height,
                                MINIGAME_INNER_TOP_OFFSET, 0, MINIGAME_INNER_LEFT_OFFSET, 0, TEXTURE_LIQUID_SKY.textureWidth, TEXTURE_LIQUID_SKY.textureHeight);
                    }
                    yMeter[3] = new DrawableResource(TEXTURE_OVERLAYS.texture, DRAWING_SEA_LEVEL.u, DRAWING_SEA_LEVEL.v, DRAWING_SEA_LEVEL.width, DRAWING_SEA_LEVEL.height,
                            1 + (MAX_Y_LEVEL - (YMETER_SEA / 5)), 0, YMETER_LEVEL_LEFT_OFFSET, 0, TEXTURE_OVERLAYS.textureWidth, TEXTURE_OVERLAYS.textureHeight);
                }
            }
        }
        
        public void drawMiniGame(@NotNull Minecraft minecraft, int x, int y) {
            minigameInner[0].draw(minecraft, x, y); // Liquid
            minigameInner[1].draw(minecraft, x, y); // Time (sky)
            minigameInner[2].draw(minecraft, x, y); // Surroundings
            minigameInner[3].draw(minecraft, x, y); // Rain
            minigameInner[4].draw(minecraft, x, y); // Thunderstorm
            minigameBorder.draw(minecraft, x, y); // Border
        }
        
        public void drawYmeter(@NotNull Minecraft minecraft, int x, int y) {
            // Background
            yMeter[0].draw(minecraft, x, y);
            // Range
            yMeter[1].draw(minecraft, x, y);
            // Foreground
            yMeter[2].draw(minecraft, x, y);
            // Sea / Lava Ocean / Void level
            yMeter[3].draw(minecraft, x, y );
        }
    }

    // INTELLIJ LAG MACHINE
    private static final short minigameBackgroundBarWidth = 128;
    private static final short minigameBackgroundBarHeight = 24;

    private static final short REEL_BASIC_RANGE = 40;
    private static final short REEL_FAST_RANGE = 75;
    private static final short REEL_LONG_RANGE = 150;
    private static final int MINIGAME_X_START = 26 - 6;
    private static final short X_OFFSET = -8;
    private static final short Y_OFFSET = -6;
    private static final int MINIGAME_Y_START = 8 + (Y_OFFSET >>1);
    private static final int MINIGAME_INNER_LEFT_OFFSET = 3;
    private static final int MINIGAME_INNER_TOP_OFFSET = 3;
    private static final int YMETER_X_START = 16 + (X_OFFSET);
    private static final int YMETER_Y_START = 8 + (Y_OFFSET >>1);
    private static final int YMETER_RANGE_LEFT_OFFSET = 1;
    private static final int YMETER_RANGE_TOP_OFFSET = 1;
    private static final int YMETER_LEVEL_LEFT_OFFSET = -1;
    private static final short MAX_Y_LEVEL = 28;
    private static final short YMETER_LAVA_OCEAN = 31;
    private static final short YMETER_VOID = 5;
    private static final short YMETER_SEA = 62;


    public static final Texture TEXTURE_MINIGAME_OUTLINE = new Texture(new ResourceLocation("fishingmadebetter", "textures/gui/reeling_hud_outline.png"), 256, 256);
    public static final Texture TEXTURE_LIQUID_SKY = new Texture(new ResourceLocation("fishingmadebetter", "textures/gui/reeling_hud_underoverlay.png"), 256, 256);
    public static final Texture TEXTURE_BIOME = new Texture(new ResourceLocation("fishingmadebetter", "textures/gui/reeling_hud_biome.png"), 256, 256);
    public static final Texture TEXTURE_DIMENSION_CAVE = new Texture(new ResourceLocation("fishingmadebetter", "textures/gui/reeling_hud_fullsize.png"), 256, 256);
    public static final Texture TEXTURE_OVERLAYS = new Texture(new ResourceLocation("fishingmadebetter", "textures/gui/fish_data_overlays.png"), 256, 256);
    public static final Drawing DRAWING_OUTLINE = new Drawing(0, 0, 133 + 1, 29 + 1);
    public static final Drawing DRAWING_THE_END = new Drawing(0, 0, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_NETHER = new Drawing(0, 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final List<Drawing> DRAWING_BIOME_LIQUIDS = Arrays.asList(
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("WATER") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("JUNGLE") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("SANDY") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("COLD") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("MOUNTAIN") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("FOREST") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("PLAINS") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("SWAMP") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("MUSHROOM") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(128, FishRequirementData.texturePosFromBiomeTag("DEAD") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight));
    public static final List<Drawing> DRAWING_BIOME_SURROUNDINGS = Arrays.asList(
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("WATER") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("JUNGLE") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("SANDY") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("COLD") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("MOUNTAIN") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("FOREST") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("PLAINS") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("SWAMP") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("MUSHROOM") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight),
            new Drawing(0, FishRequirementData.texturePosFromBiomeTag("DEAD") * 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight));
    public static final Drawing DRAWING_LIQUID_LAVA = new Drawing(0, 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_LIQUID_VOID = new Drawing(0, 48, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_LIQUID_ANY = new Drawing(128, 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_DAY = new Drawing(0, 72, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_NIGHT = new Drawing(0, 96, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_DAY_NIGHT = new Drawing(128, 0, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_TIMELESS = new Drawing(0, 120, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_RAIN = new Drawing(0, 0, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_THUNDERSTORM = new Drawing(0, 24, minigameBackgroundBarWidth, minigameBackgroundBarHeight);
    public static final Drawing DRAWING_YMETER_BACKGROUND = new Drawing(0, 49, 6 + 1, 29 + 1);
    public static final Drawing DRAWING_YMETER_FOREGROUND = new Drawing(8, 49, 6 + 1, 29 + 1);
    public static final Drawing DRAWING_YRANGE = new Drawing(16, 49, 4 + 1, 27 + 1);
    public static final Drawing DRAWING_SEA_LEVEL = new Drawing(22, 49, 8 + 1, 1);
    public static final Drawing DRAWING_LAVA_LEVEL = new Drawing(22, 51, 8 + 1, 1);
    public static final Drawing DRAWING_VOID_LEVEL = new Drawing(22, 53, 8 + 1, 1);

}

package localizator.mixin.forgottenitems;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tschipp.forgottenitems.items.ItemGolemArmor;

import java.util.List;

@Mixin(ItemGolemArmor.class)
public abstract class ItemGolemArmorMixin {
    /**
     * @author KameiB
     * @reason Localize item description
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    // Line 61
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    {        
        tooltip.add(I18n.format(((Item)((Object)this)).getUnlocalizedNameInefficiently(stack) + ".desc"));
    }
}

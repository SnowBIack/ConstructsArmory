/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Construct's Armory, a mod made for Minecraft.
 *
 * Construct's Armory is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Construct's Armory is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Construct's Armory.  If not, see <https://www.gnu.org/licenses/>.
 */

package c4.conarm.common.armor.traits;

import c4.conarm.common.armor.utils.ArmorTagUtil;
import c4.conarm.lib.armor.ArmorNBT;
import c4.conarm.lib.materials.CoreMaterialStats;
import c4.conarm.lib.materials.PlatesMaterialStats;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.utils.TagUtil;

import java.util.List;

public class TraitAlien extends TraitProgressiveArmorStats {

    protected static final int TICK_PER_STAT = 72;

    protected static final int DURABILITY_STEP = 1;
    protected static final float TOUGHNESS_STEP = 0.01F;
    protected static final float DEFENSE_STEP = 0.02F;

    public TraitAlien() {
        super("alien", TextFormatting.YELLOW);
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {

        if (!hasPool(rootCompound)) {

            StatNBT data = new StatNBT();

            int statPoints = 800;
            for (; statPoints > 0; statPoints--) {
                switch (random.nextInt(3)) {
                    case 0:
                        data.durability += DURABILITY_STEP;
                        break;
                    case 1:
                        data.toughness += TOUGHNESS_STEP;
                        break;
                    case 2:
                        data.defense += DEFENSE_STEP;
                        break;
                }
            }

            setPool(rootCompound, data);
        }

        super.applyEffect(rootCompound, modifierTag);
    }

    @Override
    public void onArmorTick(ItemStack armor, World world, EntityPlayer player){

        if (player.world.isRemote) {
            return;
        }

        if (player.ticksExisted % TICK_PER_STAT > 0) {
            return;
        }

        NBTTagCompound root = TagUtil.getTagSafe(armor);
        StatNBT pool = getPool(root);
        StatNBT distributed = getBonus(root);
        ArmorNBT data = ArmorTagUtil.getArmorStats(armor);

        //Defense
        if (player.ticksExisted % (TICK_PER_STAT * 3) == 0) {
            if (distributed.defense < pool.defense) {
                data.defense += DEFENSE_STEP;
                distributed.defense += DEFENSE_STEP;
            }
        }

        //Toughness
        else if (player.ticksExisted % (TICK_PER_STAT * 2) == 0) {
            if (distributed.toughness < pool.toughness) {
                data.toughness += TOUGHNESS_STEP;
                distributed.toughness += TOUGHNESS_STEP;
            }
        }
        //Durability
        else {
            if (distributed.durability < pool.durability) {
                data.durability += DURABILITY_STEP;
                distributed.durability += DURABILITY_STEP;
            }
        }

        TagUtil.setToolTag(root, data.get());
        setBonus(root, distributed);
    }

    @Override
    public List<String> getExtraInfo(ItemStack tool, NBTTagCompound modifierTag) {
        StatNBT pool = getBonus(TagUtil.getTagSafe(tool));

        return ImmutableList.of(CoreMaterialStats.formatDurability(pool.durability),
                CoreMaterialStats.formatDefense(pool.defense),
                PlatesMaterialStats.formatToughness(pool.toughness));
    }
}

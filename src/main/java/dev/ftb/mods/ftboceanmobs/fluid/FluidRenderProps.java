/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ftb.mods.ftboceanmobs.fluid;

import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class FluidRenderProps implements IClientFluidTypeExtensions {
    private final ResourceLocation still;
    private final ResourceLocation flowing;
    private final int colorTint;

    public FluidRenderProps(String still, String flowing) {
        this(still, flowing, 0xFFFFFFFF);
    }

    public FluidRenderProps(String still, String flowing, int colorTint) {
        this.still = still.indexOf(':') > 0 ? ResourceLocation.parse(still) : FTBOceanMobs.id("block/fluid/" + still);
        this.flowing = flowing.indexOf(':') > 0 ? ResourceLocation.parse(flowing) : FTBOceanMobs.id("block/fluid/" + flowing);
        this.colorTint = colorTint;
    }

    @Override
    public ResourceLocation getStillTexture() {
        return still;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return flowing;
    }

    @Override
    public int getTintColor() {
        return colorTint;
    }
}

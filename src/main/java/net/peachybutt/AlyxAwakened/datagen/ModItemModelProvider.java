package net.peachybutt.AlyxAwakened.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.peachybutt.AlyxAwakened.AlyxAwakened;
import net.peachybutt.AlyxAwakened.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AlyxAwakened.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.INSERTNAME);
        simpleItem(ModItems.UNOBTANIUM);
        simpleItem(ModItems.ALYX_SPAWN_EGG);

        withExistingParent(ModItems.ALYX_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(AlyxAwakened.MOD_ID,"item/" + item.getId().getPath()));
    }
}

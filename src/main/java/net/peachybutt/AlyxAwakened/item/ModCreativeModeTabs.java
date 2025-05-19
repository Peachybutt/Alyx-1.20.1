package net.peachybutt.AlyxAwakened.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.peachybutt.AlyxAwakened.AlyxAwakened;
import net.peachybutt.AlyxAwakened.block.ModBlocks;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlyxAwakened.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ALYX_TAB = CREATIVE_MODE_TABS.register("alyx_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.INSERTNAME.get()))
                    .title(Component.translatable("creativetab.alyx_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.INSERTNAME.get());
                        pOutput.accept(ModItems.UNOBTANIUM.get());
                        pOutput.accept(ModItems.ALYX_SPAWN_EGG.get());

                        pOutput.accept(ModBlocks.UNOBTANIUM_BLOCK.get());
                        pOutput.accept(ModBlocks.ANOTHER_BLOCK.get());
                        pOutput.accept(ModBlocks.DEEPSLATE_UNOBTANIUM_ORE.get());
                        pOutput.accept(ModBlocks.UNOBTANIUM_ORE.get());
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}

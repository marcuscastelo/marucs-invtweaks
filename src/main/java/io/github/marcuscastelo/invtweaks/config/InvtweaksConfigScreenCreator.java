//package io.github.marcuscastelo.invtweaks.config;
//
//import me.shedaniel.clothconfig2.api.ConfigBuilder;
//import me.shedaniel.clothconfig2.api.ConfigCategory;
//import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
//import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
//import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.item.Item;
//import net.minecraft.text.LiteralText;
//import net.minecraft.text.TranslatableText;
//
//import java.util.Arrays;
//import java.util.stream.Collectors;
//
//public class InvtweaksConfigScreenCreator {
//    public static Screen createConfigScreen(Screen parent) {
//        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new LiteralText("InvTweaks"));
//
//        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.marucs-invtweaks.general"));
//        var dropdownEntry = buildOverflowModeDropdownEntry(builder.entryBuilder());
//
//        general.addEntry(dropdownEntry);
//
//        return builder.build();
//    }
//
//    private static DropdownBoxEntry<?> buildOverflowModeDropdownEntry(ConfigEntryBuilder entryBuilder) {
//
//        String value = "A";
//
//        var dropDownBuilder = entryBuilder.startDropdownMenu(new TranslatableText("marucs-invtweaks.overflowMode"), "A", a -> a);
//        dropDownBuilder.setDefaultValue("A");
//        dropDownBuilder.setTooltip(new LiteralText("marucs-invtweaks.overflowMode.tooltip"));
//        dropDownBuilder.setSaveConsumer(a -> System.out.println(a));
//        dropDownBuilder.setSelections(Arrays.stream(new String[]{"A", "B", "C"}).collect(Collectors.toList()));
//
//        return dropDownBuilder.build();
//    }
//}

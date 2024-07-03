package cn.ksmcbrigade.mpwl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mod("mpwl")
public class ModPackWhiteList {

    private File config = new File("config/mpwl-config.json");

    public ModPackWhiteList(){
        new File("config").mkdirs();
        if(!config.exists()){
            JsonArray array = new JsonArray();
            ModList.get().getMods().forEach(m -> array.add(m.getModId()));
            try {
                Files.writeString(config.toPath(),array.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            JsonArray list = JsonParser.parseString(Files.readString(config.toPath())).getAsJsonArray();
            JsonArray client = new JsonArray();
            ModList.get().getMods().forEach(m -> client.add(m.getModId()));

            List<String>[] lists = getInJsonArrays(list,client);

            if(!lists[0].isEmpty() || !lists[1].isEmpty()){

                if(Boolean.parseBoolean(System.getProperty("java.awt.headless"))) System.setProperty("java.awt.headless","false");

                JOptionPane.showMessageDialog(null,getMessage(lists),"ModPackWhiteList",JOptionPane.WARNING_MESSAGE);

                System.exit(1);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String>[] getInJsonArrays(JsonArray array1, JsonArray array2) {
        List<String> missingInArr2 = new ArrayList<>();
        List<String> extraInArr2 = new ArrayList<>();

        for (JsonElement elem1 : array1) {
            if (!array2.contains(elem1)) {
                missingInArr2.add(elem1.getAsString());
            }
        }

        for (JsonElement elem2 : array2) {
            if (!array1.contains(elem2)) {
                extraInArr2.add(elem2.getAsString());
            }
        }

        return new List[]{missingInArr2,extraInArr2};
    }

    public static String getMessage(List<String>[] lists){

        StringBuilder builder = new StringBuilder();

        Locale locale = Locale.getDefault();

        if(locale.getLanguage().equalsIgnoreCase(Locale.CHINA.getLanguage())){
            builder.append("发现当前已搭载的模组与已配置的模组白名单不一致，需要做出以下更改：").append("\n");
        }
        else{
            builder.append("If the current mods installed is inconsistent with the configured mods whitelist, you need to make the following changes: ").append("\n");
        }

        boolean l = false;
        if(!lists[0].isEmpty()){
            l = true;
            if(locale.getLanguage().equalsIgnoreCase(Locale.CHINA.getLanguage())){
                builder.append("需要安装：");
            }
            else{
                builder.append("Need to install: ");
            }
            for(String obj:lists[0]){
                builder.append(obj).append("(modId)").append(", ");
            }

            builder.deleteCharAt(builder.length() - 2);
        }
        if(!lists[1].isEmpty()){
            if(l) builder.append("\n");
            if(locale.getLanguage().equalsIgnoreCase(Locale.CHINA.getLanguage())){
                builder.append("需要禁用：");
            }
            else{
                builder.append("Need to disable: ");
            }
            for(String obj:lists[1]){
                IModInfo mod = getMod(obj);
                if(mod==null) continue;
                builder.append(mod.getDisplayName()).append("(").append(obj).append(")").append(", ");
            }

            builder.deleteCharAt(builder.length() - 2);
        }

        return builder.toString();
    }

    public static @Nullable IModInfo getMod(String id){
        for(IModInfo info:ModList.get().getMods()){
            if(info.getModId().toLowerCase()==id.toLowerCase()){
                return info;
            }
        }
        return null;
    }
}

package me.dexuby.UltimateDrugs.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializationUtils {
   public static List<String> serializeLocationSet(Set<Location> var0) {
      ArrayList var1 = new ArrayList();

      try {
         for(Location var3 : var0) {
            ByteArrayOutputStream var4 = new ByteArrayOutputStream();
            BukkitObjectOutputStream var5 = new BukkitObjectOutputStream(var4);
            var5.writeObject(var3);
            var5.close();
            var1.add(Base64Coder.encodeLines(var4.toByteArray()));
         }

         return var1;
      } catch (IOException var6) {
         var6.printStackTrace();
         return var1;
      }
   }

   public static Set<Location> deserializeLocationSet(List<String> var0) {
      HashSet var1 = new HashSet();

      try {
         for(String var3 : var0) {
            ByteArrayInputStream var4 = new ByteArrayInputStream(Base64Coder.decodeLines(var3));
            BukkitObjectInputStream var5 = new BukkitObjectInputStream(var4);
            Location var6 = (Location)var5.readObject();
            var5.close();
            var1.add(var6);
         }

         return var1;
      } catch (ClassNotFoundException | IOException var7) {
         ((Exception)var7).printStackTrace();
         return var1;
      }
   }
}

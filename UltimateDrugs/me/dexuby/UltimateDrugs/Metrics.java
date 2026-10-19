package me.dexuby.UltimateDrugs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Metrics {
   public static final int B_STATS_VERSION = 1;
   private static final String URL = "https://bStats.org/submitData/bukkit";
   private boolean enabled;
   private static boolean logFailedRequests;
   private static boolean logSentData;
   private static boolean logResponseStatusText;
   private static String serverUUID;
   private final Plugin plugin;
   private final List<CustomChart> charts = new ArrayList();

   public Metrics(Plugin var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("Plugin cannot be null!");
      } else {
         this.plugin = var1;
         File var2 = new File(var1.getDataFolder().getParentFile(), "bStats");
         File var3 = new File(var2, "config.yml");
         YamlConfiguration var4 = YamlConfiguration.loadConfiguration(var3);
         if (!var4.isSet("serverUuid")) {
            var4.addDefault("enabled", true);
            var4.addDefault("serverUuid", UUID.randomUUID().toString());
            var4.addDefault("logFailedRequests", false);
            var4.addDefault("logSentData", false);
            var4.addDefault("logResponseStatusText", false);
            var4.options().header("bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)").copyDefaults(true);

            try {
               var4.save(var3);
            } catch (IOException var9) {
            }
         }

         this.enabled = var4.getBoolean("enabled", true);
         serverUUID = var4.getString("serverUuid");
         logFailedRequests = var4.getBoolean("logFailedRequests", false);
         logSentData = var4.getBoolean("logSentData", false);
         logResponseStatusText = var4.getBoolean("logResponseStatusText", false);
         if (this.enabled) {
            boolean var5 = false;

            for(Class var7 : Bukkit.getServicesManager().getKnownServices()) {
               try {
                  var7.getField("B_STATS_VERSION");
                  var5 = true;
                  break;
               } catch (NoSuchFieldException var10) {
               }
            }

            Bukkit.getServicesManager().register(Metrics.class, this, var1, ServicePriority.Normal);
            if (!var5) {
               this.startSubmitting();
            }
         }

      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void addCustomChart(CustomChart var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("Chart cannot be null!");
      } else {
         this.charts.add(var1);
      }
   }

   private void startSubmitting() {
      final Timer var1 = new Timer(true);
      var1.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            if (!Metrics.this.plugin.isEnabled()) {
               var1.cancel();
            } else {
               Bukkit.getScheduler().runTask(Metrics.this.plugin, () -> Metrics.this.submitData());
            }
         }
      }, 300000L, 1800000L);
   }

   public JSONObject getPluginData() {
      JSONObject var1 = new JSONObject();
      String var2 = this.plugin.getDescription().getName();
      String var3 = this.plugin.getDescription().getVersion();
      var1.put("pluginName", var2);
      var1.put("pluginVersion", var3);
      JSONArray var4 = new JSONArray();

      for(CustomChart var6 : this.charts) {
         JSONObject var7 = var6.getRequestJsonObject();
         if (var7 != null) {
            var4.add(var7);
         }
      }

      var1.put("customCharts", var4);
      return var1;
   }

   private JSONObject getServerData() {
      int var1;
      try {
         Method var2 = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
         var1 = var2.getReturnType().equals(Collection.class) ? ((Collection)var2.invoke(Bukkit.getServer())).size() : ((Player[])((Player[])var2.invoke(Bukkit.getServer()))).length;
      } catch (Exception var10) {
         var1 = Bukkit.getOnlinePlayers().size();
      }

      int var11 = Bukkit.getOnlineMode() ? 1 : 0;
      String var3 = Bukkit.getVersion();
      String var4 = System.getProperty("java.version");
      String var5 = System.getProperty("os.name");
      String var6 = System.getProperty("os.arch");
      String var7 = System.getProperty("os.version");
      int var8 = Runtime.getRuntime().availableProcessors();
      JSONObject var9 = new JSONObject();
      var9.put("serverUUID", serverUUID);
      var9.put("playerAmount", var1);
      var9.put("onlineMode", var11);
      var9.put("bukkitVersion", var3);
      var9.put("javaVersion", var4);
      var9.put("osName", var5);
      var9.put("osArch", var6);
      var9.put("osVersion", var7);
      var9.put("coreCount", var8);
      return var9;
   }

   private void submitData() {
      JSONObject var1 = this.getServerData();
      JSONArray var2 = new JSONArray();

      for(Class var4 : Bukkit.getServicesManager().getKnownServices()) {
         try {
            var4.getField("B_STATS_VERSION");

            for(RegisteredServiceProvider var6 : Bukkit.getServicesManager().getRegistrations(var4)) {
               try {
                  var2.add(var6.getService().getMethod("getPluginData").invoke(var6.getProvider()));
               } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException var8) {
               }
            }
         } catch (NoSuchFieldException var9) {
         }
      }

      var1.put("plugins", var2);
      (new Thread(() -> {
         try {
            sendData(this.plugin, var1);
         } catch (Exception var3) {
            if (logFailedRequests) {
               this.plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + this.plugin.getName(), var3);
            }
         }

      })).start();
   }

   private static void sendData(Plugin var0, JSONObject var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("Data cannot be null!");
      } else if (Bukkit.isPrimaryThread()) {
         throw new IllegalAccessException("This method must not be called from the main thread!");
      } else {
         if (logSentData) {
            var0.getLogger().info("Sending data to bStats: " + var1.toString());
         }

         HttpsURLConnection var2 = (HttpsURLConnection)(new URL("https://bStats.org/submitData/bukkit")).openConnection();
         byte[] var3 = compress(var1.toString());
         var2.setRequestMethod("POST");
         var2.addRequestProperty("Accept", "application/json");
         var2.addRequestProperty("Connection", "close");
         var2.addRequestProperty("Content-Encoding", "gzip");
         var2.addRequestProperty("Content-Length", String.valueOf(var3.length));
         var2.setRequestProperty("Content-Type", "application/json");
         var2.setRequestProperty("User-Agent", "MC-Server/1");
         var2.setDoOutput(true);
         DataOutputStream var4 = new DataOutputStream(var2.getOutputStream());
         var4.write(var3);
         var4.flush();
         var4.close();
         InputStream var5 = var2.getInputStream();
         BufferedReader var6 = new BufferedReader(new InputStreamReader(var5));
         StringBuilder var7 = new StringBuilder();

         String var8;
         while((var8 = var6.readLine()) != null) {
            var7.append(var8);
         }

         var6.close();
         if (logResponseStatusText) {
            var0.getLogger().info("Sent data to bStats and received response: " + var7.toString());
         }

      }
   }

   private static byte[] compress(String var0) {
      if (var0 == null) {
         return null;
      } else {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         GZIPOutputStream var2 = new GZIPOutputStream(var1);
         var2.write(var0.getBytes(StandardCharsets.UTF_8));
         var2.close();
         return var1.toByteArray();
      }
   }

   static {
      if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
         String var0 = new String(new byte[]{111, 114, 103, 46, 98, 115, 116, 97, 116, 115, 46, 98, 117, 107, 107, 105, 116});
         String var1 = new String(new byte[]{121, 111, 117, 114, 46, 112, 97, 99, 107, 97, 103, 101});
         if (Metrics.class.getPackage().getName().equals(var0) || Metrics.class.getPackage().getName().equals(var1)) {
            throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
         }
      }

   }

   public abstract static class CustomChart {
      final String chartId;

      CustomChart(String var1) {
         if (var1 != null && !var1.isEmpty()) {
            this.chartId = var1;
         } else {
            throw new IllegalArgumentException("ChartId cannot be null or empty!");
         }
      }

      private JSONObject getRequestJsonObject() {
         JSONObject var1 = new JSONObject();
         var1.put("chartId", this.chartId);

         try {
            JSONObject var2 = this.getChartData();
            if (var2 == null) {
               return null;
            } else {
               var1.put("data", var2);
               return var1;
            }
         } catch (Throwable var3) {
            if (Metrics.logFailedRequests) {
               Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id " + this.chartId, var3);
            }

            return null;
         }
      }

      protected abstract JSONObject getChartData();
   }

   public static class SimplePie extends CustomChart {
      private final Callable<String> callable;

      public SimplePie(String var1, Callable<String> var2) {
         super(var1);
         this.callable = var2;
      }

      protected JSONObject getChartData() {
         JSONObject var1 = new JSONObject();
         String var2 = (String)this.callable.call();
         if (var2 != null && !var2.isEmpty()) {
            var1.put("value", var2);
            return var1;
         } else {
            return null;
         }
      }
   }

   public static class AdvancedPie extends CustomChart {
      private final Callable<Map<String, Integer>> callable;

      public AdvancedPie(String var1, Callable<Map<String, Integer>> var2) {
         super(var1);
         this.callable = var2;
      }

      protected JSONObject getChartData() {
         JSONObject var1 = new JSONObject();
         JSONObject var2 = new JSONObject();
         Map var3 = (Map)this.callable.call();
         if (var3 != null && !var3.isEmpty()) {
            boolean var4 = true;

            for(Map.Entry var6 : var3.entrySet()) {
               if ((Integer)var6.getValue() != 0) {
                  var4 = false;
                  var2.put(var6.getKey(), var6.getValue());
               }
            }

            if (var4) {
               return null;
            } else {
               var1.put("values", var2);
               return var1;
            }
         } else {
            return null;
         }
      }
   }

   public static class DrilldownPie extends CustomChart {
      private final Callable<Map<String, Map<String, Integer>>> callable;

      public DrilldownPie(String var1, Callable<Map<String, Map<String, Integer>>> var2) {
         super(var1);
         this.callable = var2;
      }

      public JSONObject getChartData() {
         JSONObject var1 = new JSONObject();
         JSONObject var2 = new JSONObject();
         Map var3 = (Map)this.callable.call();
         if (var3 != null && !var3.isEmpty()) {
            boolean var4 = true;

            for(Map.Entry var6 : var3.entrySet()) {
               JSONObject var7 = new JSONObject();
               boolean var8 = true;

               for(Map.Entry var10 : ((Map)var3.get(var6.getKey())).entrySet()) {
                  var7.put(var10.getKey(), var10.getValue());
                  var8 = false;
               }

               if (!var8) {
                  var4 = false;
                  var2.put(var6.getKey(), var7);
               }
            }

            if (var4) {
               return null;
            } else {
               var1.put("values", var2);
               return var1;
            }
         } else {
            return null;
         }
      }
   }

   public static class SingleLineChart extends CustomChart {
      private final Callable<Integer> callable;

      public SingleLineChart(String var1, Callable<Integer> var2) {
         super(var1);
         this.callable = var2;
      }

      protected JSONObject getChartData() {
         JSONObject var1 = new JSONObject();
         int var2 = (Integer)this.callable.call();
         if (var2 == 0) {
            return null;
         } else {
            var1.put("value", var2);
            return var1;
         }
      }
   }

   public static class MultiLineChart extends CustomChart {
      private final Callable<Map<String, Integer>> callable;

      public MultiLineChart(String var1, Callable<Map<String, Integer>> var2) {
         super(var1);
         this.callable = var2;
      }

      protected JSONObject getChartData() {
         JSONObject var1 = new JSONObject();
         JSONObject var2 = new JSONObject();
         Map var3 = (Map)this.callable.call();
         if (var3 != null && !var3.isEmpty()) {
            boolean var4 = true;

            for(Map.Entry var6 : var3.entrySet()) {
               if ((Integer)var6.getValue() != 0) {
                  var4 = false;
                  var2.put(var6.getKey(), var6.getValue());
               }
            }

            if (var4) {
               return null;
            } else {
               var1.put("values", var2);
               return var1;
            }
         } else {
            return null;
         }
      }
   }

   public static class SimpleBarChart extends CustomChart {
      private final Callable<Map<String, Integer>> callable;

      public SimpleBarChart(String var1, Callable<Map<String, Integer>> var2) {
         super(var1);
         this.callable = var2;
      }

      protected JSONObject getChartData() {
         JSONObject var1 = new JSONObject();
         JSONObject var2 = new JSONObject();
         Map var3 = (Map)this.callable.call();
         if (var3 != null && !var3.isEmpty()) {
            for(Map.Entry var5 : var3.entrySet()) {
               JSONArray var6 = new JSONArray();
               var6.add(var5.getValue());
               var2.put(var5.getKey(), var6);
            }

            var1.put("values", var2);
            return var1;
         } else {
            return null;
         }
      }
   }

   public static class AdvancedBarChart extends CustomChart {
      private final Callable<Map<String, int[]>> callable;

      public AdvancedBarChart(String var1, Callable<Map<String, int[]>> var2) {
         super(var1);
         this.callable = var2;
      }

      protected JSONObject getChartData() {
         JSONObject var1 = new JSONObject();
         JSONObject var2 = new JSONObject();
         Map var3 = (Map)this.callable.call();
         if (var3 != null && !var3.isEmpty()) {
            boolean var4 = true;

            for(Map.Entry var6 : var3.entrySet()) {
               if (((int[])var6.getValue()).length != 0) {
                  var4 = false;
                  JSONArray var7 = new JSONArray();

                  for(int var11 : (int[])var6.getValue()) {
                     var7.add(var11);
                  }

                  var2.put(var6.getKey(), var7);
               }
            }

            if (var4) {
               return null;
            } else {
               var1.put("values", var2);
               return var1;
            }
         } else {
            return null;
         }
      }
   }
}

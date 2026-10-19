package me.dexuby.UltimateDrugs.drugs.chatslur;

import me.dexuby.UltimateDrugs.drugs.chatslur.strategies.ChatSlurStrategy;

public class ChatSlurConverter {
   private String chatMessage;
   private ChatSlurStrategy slurStrategy;

   public ChatSlurConverter(String var1) {
      this.chatMessage = var1;
   }

   public ChatSlurConverter setSlurStrategy(ChatSlurStrategy var1) {
      this.slurStrategy = var1;
      return this;
   }

   public String convert() {
      return this.slurStrategy != null ? this.slurStrategy.convert(this.chatMessage) : this.chatMessage;
   }
}

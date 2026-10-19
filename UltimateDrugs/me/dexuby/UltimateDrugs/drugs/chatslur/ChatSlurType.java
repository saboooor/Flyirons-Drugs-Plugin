package me.dexuby.UltimateDrugs.drugs.chatslur;

import me.dexuby.UltimateDrugs.drugs.chatslur.strategies.ChatSlurStrategy;
import me.dexuby.UltimateDrugs.drugs.chatslur.strategies.SmileysChatSlurStrategy;
import me.dexuby.UltimateDrugs.drugs.chatslur.strategies.StutterChatSlurStrategy;

public enum ChatSlurType {
   NONE((ChatSlurStrategy)null),
   SMILEYS(new SmileysChatSlurStrategy()),
   STUTTER(new StutterChatSlurStrategy());

   private ChatSlurStrategy slurStrategy;

   private ChatSlurType(ChatSlurStrategy var3) {
      this.slurStrategy = var3;
   }

   public ChatSlurStrategy getChatSlurStrategy() {
      return this.slurStrategy;
   }
}

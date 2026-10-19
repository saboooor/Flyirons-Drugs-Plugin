package me.dexuby.UltimateDrugs.drugs;

public class DrugDealer {
   private int npcId;
   private DrugDealerType type;

   public DrugDealer(int var1, DrugDealerType var2) {
      this.npcId = var1;
      this.type = var2;
   }

   public int getNpcId() {
      return this.npcId;
   }

   public DrugDealerType getType() {
      return this.type;
   }
}

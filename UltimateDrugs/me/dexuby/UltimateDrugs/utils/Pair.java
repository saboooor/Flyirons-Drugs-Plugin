package me.dexuby.UltimateDrugs.utils;

public class Pair<K, V> {
   private final K key;
   private final V value;

   public Pair(K var1, V var2) {
      this.key = var1;
      this.value = var2;
   }

   public K getKey() {
      return this.key;
   }

   public V getValue() {
      return this.value;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof Pair)) {
         return false;
      } else {
         Pair var2 = (Pair)var1;
         return this.key.equals(var2.key) && this.value.equals(var2.value);
      }
   }

   public int hashCode() {
      return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
   }
}

package me.ellbristow.mychunk;

public class LiteChunk {

        private String worldName;
        private int x;
        private int z;
        private String owner;
        private boolean forSale;
        
        public LiteChunk(String worldName, int x, int z, String owner, Boolean forSale) {
            this.worldName = worldName;
            this.x = x;
            this.z = z;
            this.owner = owner;
            this.forSale = forSale;
        }
        
        public String getWorldName() {
            return worldName;
        }
        
        public String getOwner() {
            return owner;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }
        
        public boolean isForSale() {
            return forSale;
        }
        
}

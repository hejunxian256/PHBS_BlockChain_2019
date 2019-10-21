public class BlockChainNode {

    private Block block;
    private int height;
    private UTXOPool utxoPool;

    public BlockChainNode(Block block, UTXOPool utxoPool, int height) {
        this.block = block;
        this.utxoPool = utxoPool;
        this.height = height;
    }

    public Block getBlock() {
        return block;
    }

    public int getHeight() { return height; }

    public UTXOPool getUtxoPool() { return utxoPool; }
}

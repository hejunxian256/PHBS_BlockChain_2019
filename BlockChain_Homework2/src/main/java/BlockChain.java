// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BlockChain {
    public static int CUT_OFF_AGE = 10;
    public static int MaxNum=100;
    private BlockChainNode MaxHeightNode;
    private Map<byte[], BlockChainNode> NodeChain;
    private TransactionPool transactionPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {

        // initialize the genesis UTXOPool with the coinbase transaction of the genesis block
        UTXOPool genesisUTXOPool = addcoinbaseTxtoUTXOPool(new UTXOPool(), genesisBlock);
        // initialize the genesis node
        BlockChainNode genesisNode = new BlockChainNode(genesisBlock, genesisUTXOPool, 1);
        this.NodeChain = new HashMap<byte[], BlockChainNode>(){{ put(genesisBlock.getHash(), genesisNode); }};
        this.MaxHeightNode = genesisNode;
        // initialize an empty TransactionPool
        this.transactionPool = new TransactionPool();
    }
    /**
     * method addcoinbaseTxtoUTXOPool() is used to add a block's coinbase transaction to the UXTOPool
     */
    private UTXOPool addcoinbaseTxtoUTXOPool(UTXOPool utxoPool, Block block) {
        Transaction coinBaseTx = block.getCoinbase();
        utxoPool.addUTXO(new UTXO(coinBaseTx.getHash(), 0), coinBaseTx.getOutput(0));
        return utxoPool;
    }

    /** Get the maximum height block
     * If there are multiple blocks at the same height, return the oldest block in
     * getMaxHeightBlock() function.
     * */
    public Block getMaxHeightBlock() {
        return MaxHeightNode.getBlock();
    }
    public BlockChainNode getMaxHeightNode(){return MaxHeightNode;}
    public Map<byte[], BlockChainNode> getNodeChain(){return NodeChain;}

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() { return MaxHeightNode.getUtxoPool(); }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return  new TransactionPool(transactionPool);
    }

    /**
     * Add block to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at height > (maxHeight - CUT_OFF_AGE).
     *
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height <= CUT_OFF_AGE + 1. As soon as height > CUT_OFF_AGE + 1, you cannot create a new block
     * at height 2
     * return true if block is successfully added
     */

    public boolean addBlock(Block block) {
        // a valid block that can be added to the block chain should satisfy following conditions:

        /**
         * condition(1): it should have a valid parent, i.e. not a genesis block, because a new genesis
         * block won't be mined.
         */
        boolean c1 = (NodeChain.containsKey(block.getPrevBlockHash())) && (block.getPrevBlockHash() != null);
        if (!c1) {
            return false;
        }

        /**
         * condition(2): the block itself should have a valid hash value
         */
        boolean c2 = (block.getHash() != null);
        if (!c2) {
            return false;
        }

        /**
         * condition(3): all transactions in the block should be valid transactions
         */
        BlockChainNode parentNode = NodeChain.get(block.getPrevBlockHash());
        TxHandler txHandler = new TxHandler(parentNode.getUtxoPool());
        List<Transaction> validTransactions = txHandler.handleTxs(block.getTransactions());
        boolean c3 = (validTransactions.size() == block.getTransactions().size());
        if (!c3) {
            return false;
        }

        /**
         * condition(4): the height condition should be satisfied
         */
        boolean c4 = (parentNode.getHeight() + 1 > MaxHeightNode.getHeight() - CUT_OFF_AGE);
        if (!c4) {
            return false;
        }

        // if all 4 conditions are satisfied, the block can be added to the chain

        if (c1 && c2 && c3 && c4) {

            // create a new BlockChainNode
            txHandler.handleTxs(block.getTransactions());
            BlockChainNode node =
                    new BlockChainNode(block, addcoinbaseTxtoUTXOPool(txHandler.getUTXOPool(), block), parentNode.getHeight() + 1);
            // put the newly created node onto NodeChain
            NodeChain.put(block.getHash(), node);
            // update the MaxHeightMode
            if (MaxHeightNode.getHeight() < node.getHeight()) {
                MaxHeightNode = node;
            }
            // remove transactions of the newly added block from  transactionPool
            List<Transaction> txs = block.getTransactions();
            for (Transaction tx : txs) {
                transactionPool.removeTransaction(tx.getHash());
            }
            //control the number of blocks
            if (MaxHeightNode.getHeight() > MaxNum) {
                Iterator iter = NodeChain.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object key = entry.getKey();
                    Object val = entry.getValue();
                    if(((BlockChainNode)val).getHeight() <= MaxHeightNode.getHeight() - MaxNum)
                        iter.remove();
                }
                }
                return true;
            }
            return false;
        }
    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }
}
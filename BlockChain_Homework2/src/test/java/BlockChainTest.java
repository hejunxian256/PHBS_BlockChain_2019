import java.util.ArrayList;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.security.*;
import junit.framework.TestCase;
import java.security.*;
import java.util.ArrayList;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;
import java.util.Arrays;


public class BlockChainTest  {
    // create 7 addresses for the following test
    KeyPair pairA = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair pairB = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair pairC = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair pairD = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair pairE = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair pairF = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair pairG = KeyPairGenerator.getInstance("RSA").generateKeyPair();

    public BlockChainTest() throws NoSuchAlgorithmException {
    }

    @Before
    public void before() throws Exception {
    }
    @After
    public void after() throws Exception {
    }

    public void setUp() throws Exception {
        BlockChain.CUT_OFF_AGE = 10;
    }

    @Test

    /**
     * Test1: GenesisBlock() is to test whether we can create a new block chain with a genesis block
     */
    public void GenesisBlock() throws Exception {
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block maxHeightBlock = blockChain.getMaxHeightBlock();
        Transaction coinBaseTx = new Transaction(25, pairA.getPublic());

        // the highest block of the chain should be the genesis block
        assertThat(maxHeightBlock.getHash(), equalTo(genesisBlock.getHash()));
        assertThat(maxHeightBlock.getCoinbase(), equalTo(coinBaseTx));

        // the MaxHeightUXTOPool should include only 1 transaction: the coinbase transaction of genesis block
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(1));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().get(0), equalTo(new UTXO(coinBaseTx.getHash(), 0)));
    }

    @Test
    /**
     * Test2: AnotherGenesisBlock() is to test whether we can prevent another genesis block from being added to
     * the chain. Because a new genesis block won't be mined, another genesis block should not be added to the chain
     */
    public void AnotherGenesisBlock() throws Exception {
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

       // B mined another genesis block, the coinbase transaction goes to B
        Block anothergenesisBlock = new Block(null, pairB.getPublic());
        anothergenesisBlock.finalize();

        //  another genesis block should not be added to the chain
        assertThat(blockHandler.processBlock(anothergenesisBlock), equalTo(false));
    }

    @Test
    /**
     * Test3: WrongOrCorrectPrevHashBlock() is to test whether we can prevent a new block with
     * a wrong prevHash from being added to the chain.
     * For simplicity, the new block here doesn't include any transaction.
     * More complicated cases will be tested later.
     */
    public void WrongOrCorrectPrevHashBlock() throws Exception {
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        byte[] correctprevHash=blockHandler.getBlockChain().getMaxHeightBlock().getHash();

        // B mined the next block, the coinbase transaction goes to B
        // if there is something wrong with the prevHash
        byte[] wrongprevHash= Arrays.copyOf(correctprevHash,correctprevHash.length);
        wrongprevHash[0]++;
        Block wrongprevHashBlock = new Block(wrongprevHash, pairB.getPublic());
        wrongprevHashBlock.finalize();

        // block with a wrong prevHash should not be added to the chain
        assertThat(blockHandler.processBlock(wrongprevHashBlock), equalTo(false));

        // B mined the next block, the coinbase transaction goes to B
        // this time the prevHash is correct
        Block correntprevHashBlock = new Block(correctprevHash, pairB.getPublic());
        correntprevHashBlock.finalize();

        // block with a correct prevHash should be added to the chain
        assertThat(blockHandler.processBlock(correntprevHashBlock), equalTo(true));
    }

    @Test
    /**
     * Test4: SpendCoinBaseTx() is to test whether we can spend the coinbase transaction in the next block
     * mined on top of it.
     */
    public void SpendCoinBaseTx() throws Exception {
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        // A transfers 25 coins to B, claiming its coinbase transaction as input
        Transaction A_to_B = new Transaction();
        A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        A_to_B.addOutput(25, pairB.getPublic());
        A_to_B.signTx(pairA.getPrivate(),0);
        A_to_B.finalize();

        // add the above transaction into transactionPool
        blockHandler.getBlockChain().addTransaction(A_to_B);

        /**
         * assume B mined the next block(block1), the coinbase transaction goes to B
         * transaction A_to_B is included in this block
         * block1 is added on top of the genesis block(the current highest block)
         */
        Block block1=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairB.getPublic());
        block1.addTransaction(A_to_B);
        block1.finalize();
        assertThat(blockHandler.processBlock(block1), equalTo(true));

        /**
         * After block1 is added to the chain, transaction A_to_B should be removed from TransactionPool
         * TransactionPool should have 0 transaction
         */
        assertThat(blockHandler.getBlockChain().getTransactionPool().getTransactions().size(),
                equalTo(0));

        /**
         * After block1 is added to the chain, MaxHeightUTXOPool should include 2 UXTOs:
         * coinbase transaction of block1 and transaction A_to_B
         */
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO().size(),
                equalTo(2));
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(A_to_B.getHash(),0)),
                equalTo(true));
        Transaction coinBaseTx = new Transaction(25, pairB.getPublic());
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(coinBaseTx.getHash(),0)),
                equalTo(true));

    }

    @Test
    /**
     * Test5: InvalidTxBlock() is to test whether we can prevent a new block with
     * invalid transactions from being added to the chain.
     * Since in Homework1 we have already tested various kinds of invalid transactions,
     * here for simplicity we only consider double spending.
     */
    public void InvalidTxBlock() throws Exception {
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        // A transfers 25 coins to B, claiming its coinbase transaction as input
        Transaction A_to_B = new Transaction();
        A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        A_to_B.addOutput(25, pairB.getPublic());
        A_to_B.signTx(pairA.getPrivate(),0);
        A_to_B.finalize();
        // add transaction A_to_B into transactionPool
        blockHandler.getBlockChain().addTransaction(A_to_B);

        // A again transfers 25 coins to C, claiming its coinbase transaction as input,
        // which causes double spending problem.
        Transaction A_to_C = new Transaction();
        A_to_C.addInput(genesisBlock.getCoinbase().getHash(), 0);
        A_to_C.addOutput(25, pairC.getPublic());
        A_to_C.signTx(pairA.getPrivate(),0);
        A_to_C.finalize();
        // add transaction A_to_C into transactionPool
        blockHandler.getBlockChain().addTransaction(A_to_C);

        /**
         * Assume D mined the next block(block1), the coinbase transaction goes to D
         * transaction A_to_B and A_to_C are included in this block
         * Since the two transactions are double spending, they are invalid, thus block1 should not be
         * added to the chain
         */
        Block block1=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairD.getPublic());
        block1.addTransaction(A_to_B);
        block1.addTransaction(A_to_C);
        block1.finalize();
        assertThat(blockHandler.processBlock(block1), equalTo(false));

        /**
         * Because block1 can not be added to the chain, transaction A_to_B and A_to_C still remains in the
         * TransactionPool.
         * The number of transactions in TransactionPool should be 2
         */
        assertThat(blockHandler.getBlockChain().getTransactionPool().getTransactions().size(),
                equalTo(2));

    }

    @Test
    /**
     * Test6: EqualHeightBlock() is to test whether we can add multiple valid blocks at the same height.
     * Also, if there are multiple blocks at the same height, the getMaxHeightBlock() function should be able to
     * return the oldest block.
     */
    public void EqualHeightBlock() throws Exception {
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        // A transfers 25 coins to B, claiming its coinbase transaction as input
        Transaction A_to_B = new Transaction();
        A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        A_to_B.addOutput(25, pairB.getPublic());
        A_to_B.signTx(pairA.getPrivate(),0);
        A_to_B.finalize();

        // add the above transaction into transactionPool
        blockHandler.getBlockChain().addTransaction(A_to_B);

        /**
         * assume B mined the next block(block1), the coinbase transaction goes to B
         * transaction A_to_B is included in this block
         * block1 is added on top of the genesis block
         */
        Block block1=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairB.getPublic());
        block1.addTransaction(A_to_B);
        block1.finalize();
        blockHandler.processBlock(block1);

        /**
         * After block1 is added to the chain,
         * B transfers 25 coins to C, claiming its coinbase transaction as input.
         * Also, B transfers 25 coins to D, claiming Tx A_to_B as input
         */
        Transaction B_to_C = new Transaction();
        B_to_C.addInput(block1.getCoinbase().getHash(), 0);
        B_to_C.addOutput(25, pairC.getPublic());
        B_to_C.signTx(pairB.getPrivate(),0);
        B_to_C.finalize();

        Transaction B_to_D = new Transaction();
        B_to_D.addInput(A_to_B.getHash(), 0);
        B_to_D.addOutput(25, pairD.getPublic());
        B_to_D.signTx(pairB.getPrivate(),0);
        B_to_D.finalize();

        // add the above transactions into transactionPool
        blockHandler.getBlockChain().addTransaction(B_to_C);
        blockHandler.getBlockChain().addTransaction(B_to_D);

        /**
         * assume E mined the next block(block2), the coinbase transaction goes to E
         * transaction B_to_C is included in this block
         * block2 is added on top of block1
         */
        Block block2=new Block(block1.getHash(),pairE.getPublic());
        block2.addTransaction(B_to_C);
        block2.finalize();
        //blockHandler.processBlock(block2);
        assertThat(blockHandler.processBlock(block2), equalTo(true));

        /**
         * assume F also mined another block(block3), the coinbase transaction goes to F
         * transaction B_to_D is included in this block
         * block3 is also added on top of block1
         */
        Block block3=new Block(block1.getHash(),pairF.getPublic());
        block3.addTransaction(B_to_D);
        block3.finalize();
        //blockHandler.processBlock(block3);
        assertThat(blockHandler.processBlock(block3), equalTo(true));

        /**
         * Both block2 and block3 are successfully added to the chain, now the transactionPool
         * should have 0 transactions
         */
        assertThat(blockHandler.getBlockChain().getTransactionPool().getTransactions().size(),
                equalTo(0));

        /**
         * Now we have 2 blocks(block2 and block3) at the same height(height=3).
         * block2 is added to the chain first, so block2 is the older block.
         * block3 is added to the chain after block2, so block3 is the younger block.
         * the getMaxHeightBlock() function should be able to return the oldest block, i.e. block2
         */
        assertThat(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),equalTo(block2.getHash()));

        /**
         * The MaxHeightUXTOPool should be the UXTOPool of block2.
         * There are 3 transactions in the UXTOPool of block2:
         * transaction B_to_C, A_to_B and coinbase transaction of block2
         */
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO().size(),
                equalTo(3));
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(B_to_C.getHash(),0)),
                equalTo(true));
        Transaction coinBaseTx = new Transaction(25, pairE.getPublic());
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(coinBaseTx.getHash(),0)),
                equalTo(true));
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(A_to_B.getHash(),0)),
                equalTo(true));
    }

    @Test
    /**
     * Test7: BlockChainReorganization() is to test whether our chain can deal with reorganization properly.
     * We use the situation implemented in Test6.
     * In Test6, we have 2 blocks(block2 and block3) at the same height(height=3), and
     * block2 is the oldest block, so chain{genesis -> block1 -> block2} is the original main branch.
     * Under normal circumstance, new block will be added on top of block2.
     * However, if someone mined his new block(block4) and add it on top of block3,
     * then chain{genesis -> block1 -> block3 -> block4} will become the new main branch, and
     * transactions in block2 will get lost.
     */
    public void BlockChainReorganization() throws Exception {
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        // A transfers 25 coins to B, claiming its coinbase transaction as input
        Transaction A_to_B = new Transaction();
        A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        A_to_B.addOutput(25, pairB.getPublic());
        A_to_B.signTx(pairA.getPrivate(),0);
        A_to_B.finalize();

        // add the above transaction into transactionPool
        blockHandler.getBlockChain().addTransaction(A_to_B);

        /**
         * assume B mined the next block(block1), the coinbase transaction goes to B
         * transaction A_to_B is included in this block
         * block1 is added on top of the genesis block
         */
        Block block1=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairB.getPublic());
        block1.addTransaction(A_to_B);
        block1.finalize();
        blockHandler.processBlock(block1);

        /**
         * After block1 is added to the chain,
         * B transfers 25 coins to C, claiming its coinbase transaction as input.
         * Also, B transfers 25 coins to D, claiming Tx A_to_B as input
         */
        Transaction B_to_C = new Transaction();
        B_to_C.addInput(block1.getCoinbase().getHash(), 0);
        B_to_C.addOutput(25, pairC.getPublic());
        B_to_C.signTx(pairB.getPrivate(),0);
        B_to_C.finalize();

        Transaction B_to_D = new Transaction();
        B_to_D.addInput(A_to_B.getHash(), 0);
        B_to_D.addOutput(25, pairD.getPublic());
        B_to_D.signTx(pairB.getPrivate(),0);
        B_to_D.finalize();

        // add the above transactions into transactionPool
        blockHandler.getBlockChain().addTransaction(B_to_C);
        blockHandler.getBlockChain().addTransaction(B_to_D);

        /**
         * assume E mined the next block(block2), the coinbase transaction goes to E
         * transaction B_to_C is included in this block
         * block2 is added on top of block1
         */
        Block block2=new Block(block1.getHash(),pairE.getPublic());
        block2.addTransaction(B_to_C);
        block2.finalize();
        blockHandler.processBlock(block2);

        /**
         * assume F also mined another block(block3), the coinbase transaction goes to F
         * transaction B_to_D is included in this block
         * block3 is also added on top of block1
         */
        Block block3=new Block(block1.getHash(),pairF.getPublic());
        block3.addTransaction(B_to_D);
        block3.finalize();
        blockHandler.processBlock(block3);

        /**
         * Now we have 2 blocks(block2 and block3) at the same height(height=3).
         * block2 is the oldest block, so chain{genesis -> block1 -> block2} is the original main branch.
         * Assume G mined his new block(block4), and he adds block4 on top of block3.
         * For simplicity, block4 does not include transactions.
         */
        Block block4=new Block(block3.getHash(),pairG.getPublic());
        block4.finalize();
        assertThat(blockHandler.processBlock(block4), equalTo(true));

        /**
         * Now chain{genesis -> block1 -> block3 -> block4} becomes the new main branch(i.e. Reorganization)
         * The MaxHeightUXTOPool should be the UXTOPool of block4.
         * There are 4 transactions in the UXTOPool of block4:
         * transaction B_to_D, coinbase transactions of block1,block3 and block4.
         * Transactions in block2 get lost
         */
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO().size(),
                equalTo(4));
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(B_to_D.getHash(),0)),
                equalTo(true));

        Transaction coinBaseTxB = new Transaction(25, pairB.getPublic());
        Transaction coinBaseTxF = new Transaction(25, pairF.getPublic());
        Transaction coinBaseTxG = new Transaction(25, pairG.getPublic());
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(coinBaseTxB.getHash(),0)),
                equalTo(true));
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(coinBaseTxF.getHash(),0)),
                equalTo(true));
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(coinBaseTxG.getHash(),0)),
                equalTo(true));
    }
    /**
     *Test8: HeightCondition() is to test whether we can prevent a new block which does not satisfy the height
     *condition from being added to the chain.
     * For simplicity of the test, we reset CUT_OFF_AGE = 2.
     */
    @Test
    public void HeightCondition() throws Exception {

        BlockChain.CUT_OFF_AGE = 2;
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block1=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairB.getPublic());
        block1.finalize();
        assertThat(blockHandler.processBlock(block1), equalTo(true));

        Block block2=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairC.getPublic());
        block2.finalize();
        assertThat(blockHandler.processBlock(block2), equalTo(true));

        Block block3=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairD.getPublic());
        block3.finalize();
        assertThat(blockHandler.processBlock(block3), equalTo(true));

        Block block4=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairE.getPublic());
        block4.finalize();
        assertThat(blockHandler.processBlock(block4), equalTo(true));

        /**
         * Assume F mined a new block(block5) and wants to add it to the chain on top of block1,
         * the height of block5 is 3(<= MaxHeight-CUT_OFF_AGE),
         * so block5 should not be added to the chain.
         */
        Block block5=new Block(block1.getHash(),pairF.getPublic());
        block5.finalize();
        assertThat(blockHandler.processBlock(block5), equalTo(false));

        /**
         * Assume G mined a new block(block6) and wants to add it to the chain on top of block2,
         * the height of block6 is 4(> MaxHeight-CUT_OFF_AGE),
         * so block6 can be added to the chain.
         */
        Block block6=new Block(block2.getHash(),pairG.getPublic());
        block6.finalize();
        assertThat(blockHandler.processBlock(block6), equalTo(true));
    }
    /**
     *Test9: ControlBlockNum() is to test whether our mechanism can safely limit the number of blocks
     * in order to avoid memory outflow
     * For simplicity of the test, we reset MaxNum = 5.
     */
    @Test
    public void ControlBlockNum() throws Exception {

        BlockChain.MaxNum = 5;
        // A mined the genesis block, the coinbase transaction goes to A
        Block genesisBlock = new Block(null, pairA.getPublic());
        genesisBlock.finalize();
        // create a new block chain with the genesis block
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block1=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairB.getPublic());
        block1.finalize();
        assertThat(blockHandler.processBlock(block1), equalTo(true));

        Block block2=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairC.getPublic());
        block2.finalize();
        assertThat(blockHandler.processBlock(block2), equalTo(true));

        Block block3=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairD.getPublic());
        block3.finalize();
        assertThat(blockHandler.processBlock(block3), equalTo(true));

        Block block4=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairE.getPublic());
        block4.finalize();
        assertThat(blockHandler.processBlock(block4), equalTo(true));

        Block block5=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairF.getPublic());
        block5.finalize();
        assertThat(blockHandler.processBlock(block5), equalTo(true));


        /**
         * The height of the chain is now 6. Because we set the MaxNum=5, the chain will drop
         * the block with height=1, i.e. the genesis block.
         * So the chain no longer contains the genesis block.
         */
        assertThat(blockHandler.getBlockChain().getNodeChain().containsKey(genesisBlock.getHash()), equalTo(false));
        assertThat(blockHandler.getBlockChain().getNodeChain().keySet().size(), equalTo(5));

        /**
         * The number of UXTOs in MaxHeightUTXOPool is 6.
         * Although genesis block is removed from the chain, the coinbase Tx of genesis block is still in the MaxHeightUTXOPool,
         * which means the removal of genesis block does not affect normal function
         */
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO().size(),
                equalTo(6));
        Transaction coinBaseTxA = new Transaction(25, pairA.getPublic());
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(coinBaseTxA.getHash(),0)),
                equalTo(true));


        Block block6=new Block(blockHandler.getBlockChain().getMaxHeightBlock().getHash(),pairG.getPublic());
        block6.finalize();
        assertThat(blockHandler.processBlock(block6), equalTo(true));

        /**
         * The height of the chain is now 7. Because we set the MaxNum=5, the chain will drop
         * the block with height=2, i.e. block1.
         * So the chain no longer contains block1.
         */
        assertThat(blockHandler.getBlockChain().getNodeChain().containsKey(block1.getHash()), equalTo(false));
        assertThat(blockHandler.getBlockChain().getNodeChain().keySet().size(), equalTo(5));

        /**
         * Although genesis block is removed from the chain, the coinbase Tx of block1 is still in the MaxHeightUTXOPool,
         * which means the removal of genesis block does not affect normal function
         */
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().getAllUTXO().size(),
                equalTo(7));
        Transaction coinBaseTxB = new Transaction(25, pairB.getPublic());
        assertThat(blockHandler.getBlockChain().getMaxHeightUTXOPool().contains(new UTXO(coinBaseTxB.getHash(),0)),
                equalTo(true));
    }

}
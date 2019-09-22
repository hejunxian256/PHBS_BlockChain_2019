import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.security.PublicKey;
import java.util.Set;

public class TxHandler {
    public UTXOPool uPool;
    /** Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is utxoPool. This should make a defensive copy of
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */

    public TxHandler(UTXOPool uPool) {
        this.uPool = new UTXOPool(uPool);
    }

    /**
     * @return true if the following 5 conditions are satisfied:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */

    public boolean isValidTx(Transaction tx) {
        double inputSum = 0;     //the sum of values of all inputs in tx
        double outputSum = 0;    //the sum of values of all outputs in tx
        Set<UTXO> claimedUTXOs = new HashSet<UTXO>(); // a set of claimed UTXOs, for implementation of (3)

        ArrayList<Transaction.Input> inputs = tx.getInputs(); //obtain all inputs in tx
        for (Transaction.Input input:inputs){
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // Implementation of condition (1)
            if (!uPool.contains(utxo)){ return false;}

            // Implementation of condition (2)
            Transaction.Output correspondOutput = uPool.getTxOutput(utxo);
            PublicKey pk = correspondOutput.address;
            int index=inputs.indexOf(input);
            boolean flag1=Crypto.verifySignature(pk, tx.getRawDataToSign(index), input.signature);
            if (!flag1) {return false;}

            // Implementation of condition (3)
            boolean flag2=claimedUTXOs.add(utxo);
            if (!flag2){return false;}

            inputSum += correspondOutput.value;  //calculate inputSum for Implementation of (5)
        }

        //Implementation of condition (4)
        ArrayList<Transaction.Output> outputs = tx.getOutputs(); //obtain all outputs in tx
        for (Transaction.Output output:outputs){
            if (output.value<=0){return false;}
            outputSum += output.value;  //calculate outputSum for implementation of (5)
        }

        //Implementation of condition (5)
        if (outputSum > inputSum) {
            return false;
        }

        return true;  //if conditions (1)~(5) are satisfied, return true
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<Transaction>();
        for (Transaction tx:possibleTxs){
            if (isValidTx(tx)){       //check if tx is valid
                acceptedTxs.add(tx);

                //add newly created UXTOs into uPool
                ArrayList<Transaction.Output> Outputs = tx.getOutputs(); //obtain all outputs in tx
                for (Transaction.Output output:Outputs){
                    int j=Outputs.indexOf(output);
                    UTXO utxo = new UTXO(tx.getHash(), j);
                    uPool.addUTXO(utxo, output);
                }

                //remove spent UXTOs from uPool
                ArrayList<Transaction.Input> inputs = tx.getInputs(); //obtain all inputs in tx
                for (Transaction.Input input:inputs){
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    uPool.removeUTXO(utxo);
                }


            }
        }
        Transaction[] acceptedTxs_array=new Transaction[acceptedTxs.size()];
        acceptedTxs.toArray(acceptedTxs_array);
        return acceptedTxs_array;   //return all accepted Txs
    }

}

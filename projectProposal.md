## A Block Chain Based Online Comment System——Comment Chain
### _Proposal of Final Project for Block Chain and Cryptocurrencies_

**Group Member:**  
Shen Tingwei, He Junxian

### 1.	Motivation
Nowadays, we are living in a rapid-growing Internet era full of different standpoints, ideas and opinions. When we are using, no matter big social apps like Weibo and Zhihu, or online shopping apps like Taobao and Jingdong, we can find comments almost everywhere. Hundreds of thousands of comments are made on   social events, individual behavior, goods and many other different topics. We, as receivers of those comments, are also becoming more and more dependent on those comments when making our own decisions. 

However, despite its rapid development, today’s online comment system has quite a few significant shortcomings. First of all, the platform which runs and maintains the comment system, i.e. the central authority, has the ability to delete or tamper users’ comment for their own interest, which suppresses free expression of opinions. Second, there are also a number of malicious users in the online comment system. They may create many fake accounts, which seem to be independent but in fact controlled by one party, to make thousands of similar comments for or against a certain topic, or start a rumor, in order to mislead public opinion for their own interest. This phenomenon is the so called “Internet Water Army”. Some other users also misbehave by posting irrelevant, meaningless or even unreasonable remarks to the comment system just because the cost of expressing opinions is very low and they think they do not need to be responsible for what they have said. 

All the shortcomings mentioned above harm the credibility of the comment system and people who rely on the system to make their own decisions must always stay cautious. Therefore, what we want to do in this project is to construct a new decentralized mechanism for online comment system, which we believe can properly solve the shortcomings mentioned above and make the system more trustable and reliable. 
### 2.	Solution
In this project, we develop a block chain based online comment system, called the Comment Chain. To simply put, comments are put onto the block chain, in order to achieve decentralization and tamper-resistance. 

There are two main characters in our Comment Chain network: miners and users. Just like the Bitcoin network, in our Comment Chain network, there is a special currency called CommentCoin. 

Users are the ones who make comments about a certain topic. If a user wants to propose his or her comment, he or she must send out the comment to miners along with some CommentCoins, which is called the “comment fee”.

Just like how miners in the Bitcoin network handle transactions, miners in our Comment Chain network will validate proposed comments, mine new blocks with validated transactions in it and append new blocks to the end of the block chain. When a new block is successfully mined and appended to the end of the chain, the miner will get a block reward in CommentCoin. The miner can also earn CommentCoin from the comment fee. 

By implementing this block chain based comment system, we can properly address the shortcomings mentioned in Part 1 in the following ways:
* The whole system is decentralized, so there won’t be a central authority that can delete or tamper users’ comment for their own interest.
* Every proposed comment is stored on the chain and is available to everyone, so users should bear in mind that they are responsible for what they have said.If anyone want to start a rumor, he can be easily tracked when implementing certain ID certification.
* Since users need to pay some CommentCoins as comment fee to the miner if they want to propose a comment, the cost of making a comment is increased.If they still try to pay the same amount as they do in the old days when the cost is relatively low, then the miners will have little motivation to append these transaction into their block. In this way, the “Internet Water Army” phenomenon will be suppressed, because it takes a lot of CommentCoins to create fake accounts and make thousands of similar comments. Also, because people need to pay to propose their comments, they will tend to make more relevant and meaningful remarks rather than irrelevant or meaningless ones.  
### 3.Rationale
  Why do we choose to develop an online comment system based on block chain rather than other methods? First of all, no matter what mechanism we use to build the comment system, as long as it’s centralized, there is always the concern that the central authority will delete or tamper users’ comment for their own interest, so we need a decentralized, tamper-resistant mechanism. 

Also, even though we implement the decentralized and tamper-resistant mechanism, if we don’t manage to increase the cost of making comments, we still cannot suppress “Internet Water Army” phenomenon and discourage people from making irrelevant, meaningless or even unreasonable remarks. So, we need to introduce the “comment fee”, similar to the transaction fee in the Bitcoin network. 

Therefore, we think it’s suitable to adopt the block chain mechanism in constructing our online comment system. More functions and technical details will be discussed in the project paper and presentation. 

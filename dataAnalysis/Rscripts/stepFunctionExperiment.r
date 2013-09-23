rm(list=ls())

setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/dataAnalysis/Rscripts/")
logDir = "/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/logs/"


loadFiles = function(logDir){
  files = list()
  files[['stock0transactions']] = read.csv(file=paste0(logDir, "columnLog_transactionBased_stock0.csv"))
  files[['stock0roundBased']] = read.csv(file=paste0(logDir, "columnLog_roundBased_stock0.csv"))
  #files[['configParameters']] = read.csv(file=paste0(logDir, "config.csv"))
  files[['worldRoundBased']] = read.csv(file=paste0(logDir, "columnLog_worldData.csv"))
  files[['orderbook0RoundBased']] = read.csv(file=paste0(logDir, "columnLog_roundBased_orderbook(0,0).csv"))
  #files[['meta']] = read.csv(file=paste0(logDir, "meta.csv"))
  n = names(files$configParameters)
  v = unlist(files$configParameters)
  l = length(n)
  g = array(data="", l)
  for(i in seq(1,l)){
    g[i] = paste0(c(n[i], "=", v[[i]]), collapse="")
  }
  
  files[['graphPrefix']] = paste0(g, collapse=", ")
  files[['rounds']] = files$stock0roundBased$round
  return(files) 
}


makeShortTimeTradePricePlot = function(files){
  #png(filename= '/Users/halfdan/Desktop/graphsForToriumi2013_09_10/flatFundamental_30_marketMakers.png',width=1920, height=1200)
  par(mfrow=c(4,1))
  from = min(files$stock0transactions$round)
  to = max(files$stock0transactions$round)
  ymin = min(files$stock0transactions$price, files$stock0roundBased$fundamental)
  ymax = max(files$stock0transactions$price, files$stock0roundBased$fundamental)
  plot(files$stock0roundBased$fundamental, col='red', type='l', ylim=c(ymin, ymax), main="executed price")
  points(files$stock0transactions$round, files$stock0transactions$price, pch=19, cex=0.3)
  lines(files$stock0roundBased$fundamental, col="red", type="l", lwd=2)
  plot(files$orderbook0RoundBased$nTradedsInRound[-1], type="h", main="Number of trades")
  ymax = max(files$orderbook0RoundBased$nUnfilledBuyOrders, files$orderbook0RoundBased$nUnfilledSellOrders)
  ymin = min(files$orderbook0RoundBased$nUnfilledBuyOrders, files$orderbook0RoundBased$nUnfilledSellOrders)
  plot(files$orderbook0RoundBased$nUnfilledBuyOrders, type="l", col="red", ylim=c(ymin,ymax), main="number of standing buy(red)/sell(green) orders")
  lines(files$orderbook0RoundBased$nUnfilledSellOrders, type="l", col="green")
  ymax = max(files$orderbook0RoundBased$bestStandingBuyPrice, files$orderbook0RoundBased$bestStandingSellPrice)
  ymin = min(files$orderbook0RoundBased$bestStandingBuyPrice, files$orderbook0RoundBased$bestStandingSellPrice)
  plot(files$orderbook0RoundBased$bestStandingBuyPrice, col="red", 
  ylim=c(ymin,ymax), main="beet buy/sell prices", pch=19, cex=0.1)
  points(files$orderbook0RoundBased$bestStandingSellPrice, col="green", pch=18, cex=0.2)
  #dev.off()
  #from = min(which(files$stock0transactions$round == 12000))   
  #to = max(which(files$stock0transactions$round == 15500))
  #plot(files$stock0transactions$round[from:to], files$stock0transactions$price[from:to], ylim=c(9990, 10010), type="l")
  #lines(files$stock0roundBased$fundamental, col="red", type="l", lwd=2)
  #from = min(which(files$stock0transactions$round == 15000))
  #to = max(which(files$stock0transactions$round == 15100))
  #plot(files$stock0transactions$round[from:to], files$stock0transactions$price[from:to], ylim=c(9990, 10010), type="l")
  #lines(files$stock0roundBased$fundamental, col="red", type="l", lwd=2)
  
}


files = loadFiles(logDir)
makeShortTimeTradePricePlot(files)
fundamentalChanges = which(diff(files$stock0roundBased$fundamental) != 0)
fundamentalAfterStep = min(files$stock0roundBased$fundamental)
firstBuyAtNewFundamental = min(which(files$orderbook0RoundBased$bestStandingBuyPrice == fundamentalAfterStep))
firstSellAtNewFundamental = min(which(files$orderbook0RoundBased$bestStandingSellPrice == fundamentalAfterStep))
nRoundsCatchUpBuy = firstBuyAtNewFundamental - fundamentalChanges 
nRoundsCatchUpSell = firstSellAtNewFundamental - fundamentalChanges 

nRounds = length(files$stock0roundBased$round)



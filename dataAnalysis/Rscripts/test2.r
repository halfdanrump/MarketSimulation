rm(list=ls())

### For Import for SMA()

library(TTR)

### Import for postscript()
library(grDevices)

exportPlotsToFiles = FALSE

experimentName = "ConstantFundamentalSameLatency"

setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/dataAnalysis/Rscripts/")
logDir = paste0("/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/logs/", experimentName, "/")
#figuresExportDir = paste0("/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/graphs/", experimentName, "/")

loadFiles = function(logDir){
  files = list()
  files[['stock0transactions']] = read.csv(file=paste0(logDir, "columnLog_transactionBased_stock0.csv"))
  files[['stock0roundBased']] = read.csv(file=paste0(logDir, "columnLog_roundBased_stock0.csv"))
  files[['configParameters']] = read.csv(file=paste0(logDir, "config.csv"))
  files[['meta']] = read.csv(file=paste0(logDir, "meta.csv"))
  n = names(files$configParameters)
  v = unlist(files$configParameters)
  l = length(n)
  g = array(data="", l)
  for(i in seq(1,l)){
    g[i] = paste0(c(n[i], "=", v[[i]]), collapse="")
  }
  
  files[['graphPrefix']] = paste0(g, collapse=", ")
  return(files) 
}

getMeanPrice = function(files, rounds=seq(1,10000)){
  prices = array(files$stock0transactions$price)
  addedPrices = array(data=0, dim=length(rounds))
  nTrades = array(data=0, dim=length(rounds))
  round = min(rounds)
  row = min(round)
  while(round < max(rounds)){
    if(round == files$stock0transactions$round[row]){
      addedPrices[round] = addedPrices[round] + price[row]
      nTrades[round] = nTrades[round] + 1
    } else{
      round = round + 1
    }
    row = row + 1
  }
  meanPrice = addedPrices / nTrades
  return(meanPrice)
}

makeTransactionPriceScatterPlot = function(files){
  
  
  if(length(which(is.nan(meanPrice)))==0){
    if(exportPlotsToFiles){
      setEPS()
      postscript(file=paste0(files$meta$graphFolder, paste0(files$graphPrefix, "_meanTradePrices.eps")), width=9, height=7, horizontal=FALSE)
    }
    
    makePlot(files, rounds, meanPrice, 1000, length(rounds))
    
    if(exportPlotsToFiles){
      dev.off()
    }
  }
}

makePlot = function(files, rounds, meanPrice, from, to){
  par(mfrow=c(2,1))
  maxPrice = max(meanPrice[from:to], files$stock0roundBased$fundamental[from:to])
  minPrice = min(meanPrice[from:to], files$stock0roundBased$fundamental[from:to])
  par(mai=c(0,1,1,1))
  plot(rounds[from:to], meanPrice[from:to], pch=19, cex=0.2, main=experimentName, sub=files$graphPrefix, ylim=c(minPrice, maxPrice), xaxt='n')
  lines(rounds[from:to], files$stock0roundBased$fundamental[from:to], col="red", lwd=2)
  lines(rounds[from:to], SMA(meanPrice[from:to], n=200), col="green", lwd=2)
  #lines(SMA(files$stock0roundBased$fundamental[from:to]), col="green")
  text(x=median(rounds[from:to]), y=(max(meanPrice[from:to]) - min(meanPrice[from:to]))*0.9 + min(meanPrice[from:to]), labels=files$graphPrefix, cex=0.7, col="blue")
  #plot(SMA(files$stock0roundBased$tradedVolume[from:to],n=100), type="l")
  par(mai=c(1,1,0,1))
  plot(rounds[from:to], SMA(files$stock0roundBased$tradedVolume[from:to],n=200), type="l", col="red", lwd=2)
}


main = function(){
  files = loadFiles(logDir)
  makeTransactionPriceScatterPlot(files)
}

#main()
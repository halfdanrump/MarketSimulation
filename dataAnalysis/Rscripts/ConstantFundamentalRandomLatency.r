rm(list=ls())

### For Import for SMA()

library(TTR)

### Import for postscript()
library(grDevices)

exportPlotsToFiles = TRUE

setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/Rscripts/")
logDir = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalRandomLatency/"
figuresExportDir = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/graphs/"

loadFiles = function(logDir){
  files = list()
  files[['stock0transactions']] = read.csv(file=paste0(logDir, "columnLog_transactionBased_stock0.cvs"))
  files[['stock0roundBased']] = read.csv(file=paste0(logDir, "columnLog_roundBased_stock0.cvs"))
  return(files)
}

makeTransactionPriceScatterPlot = function(files){
  rounds = files$stock0roundBased$round
  
  
  meanPrice = rep(NA, length(rounds))
  for(round in rounds){
    idx = which(files$stock0transactions$round == round)
    m = mean(files$stock0transactions$price[idx])
    if(is.nan(m)){
      print(round)
      if(round == 1){
        meanPrice[1] = files$stock0roundBased$fundamental[1]
      } else{
        meanPrice[round] = meanPrice[round-1]
      }
    } else{
      meanPrice[round] = m
    }
  }
  
  if(length(which(is.nan(meanPrice)))==0){
    if(exportPlotsToFiles){
      setEPS()
      postscript(file=paste0(figuresExportDir, paste0("ConstantFundamentalRandomLatency_meanTradePrices", ".eps")), width=7, height=9, horizontal=FALSE)
    }
    
    plot(rounds, meanPrice, pch=19, cex=0.2, main="No HFTs")
    lines(rounds, files$stock0roundBased$fundamental, col="red", lwd=2)
    
    if(exportPlotsToFiles){
      dev.off()
    }
  }
}

main = function(){
  files = loadFiles(logDir)
  makeTransactionPriceScatterPlot(files=files) 
}
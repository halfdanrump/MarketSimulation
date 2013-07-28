rm(list=ls())

### For Import for SMA()

library(TTR)

### Import for postscript()
library(grDevices)

printPlots = FALSE

setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/Rscripts/")
logDir = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamental/"
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
      print(paste0(c("There were no orders in round", round, ", so the price was the same as last round.")))
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
      plot(rounds, meanPrice, pch=19, cex=0.2, main = "With zero-delay HFTs")
      lines(rounds, files$stock0roundBased$fundamental, col="red", lwd=2)
      
  }
}

main = function(){
  files = loadFiles(logDir)
  makeTransactionPriceScatterPlot(files)
}
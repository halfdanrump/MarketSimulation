rm(list=ls())

### For Import for SMA()

library(TTR)

### Import for postscript()
library(grDevices)

exportPlotsToFiles = TRUE

experimentName = "ConstantFundamentalRandomLatency"

setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/Rscripts/")
logDir = paste0("/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/", experimentName, "/")
figuresExportDir = paste0("/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/graphs/", experimentName, "/")

loadFiles = function(logDir){
  files = list()
  files[['stock0transactions']] = read.csv(file=paste0(logDir, "columnLog_transactionBased_stock0.cvs"))
  files[['stock0roundBased']] = read.csv(file=paste0(logDir, "columnLog_roundBased_stock0.cvs"))
  files[['configParameters']] = read.csv(file=paste0(logDir, "config.cvs"))
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

makeTransactionPriceScatterPlot = function(files){
  rounds = files$stock0roundBased$round
  
  
  meanPrice = rep(NA, length(rounds))
  for(round in rounds){
    idx = which(files$stock0transactions$round == round)
    m = mean(files$stock0transactions$price[idx])
    if(is.nan(m)){
      print(paste0(c("There were no transactions in round ", round, ", so the price was the same as last round."), collapse=""))
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
      postscript(file=paste0(figuresExportDir, paste0(files$graphPrefix, "_meanTradePrices.eps")), width=9, height=7, horizontal=FALSE)
    }
    
    makePlot(files, rounds, meanPrice)
    
    if(exportPlotsToFiles){
      dev.off()
    }
  }
}

makePlot = function(files, rounds, meanPrice){
  plot(rounds, meanPrice, pch=19, cex=0.2, main=experimentName, sub=files$graphPrefix)
  lines(rounds, files$stock0roundBased$fundamental, col="red", lwd=2)
  text(x=median(rounds), y=(max(meanPrice) - min(meanPrice))*0.9 + min(meanPrice), labels=files$graphPrefix, cex=0.7, col="blue")
}

main = function(){
  files = loadFiles(logDir)
  makeTransactionPriceScatterPlot(files)
}

main()
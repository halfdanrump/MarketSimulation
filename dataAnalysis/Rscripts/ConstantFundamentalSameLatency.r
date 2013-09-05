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
  files[['worldRoundBased']] = read.csv(file=paste0(logDir, "columnLog_worldData.csv"))
  files[['meta']] = read.csv(file=paste0(logDir, "meta.csv"))
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

getStats = function(files){
  rounds = files$rounds
  
  
  meanPrice = rep(NA, length(rounds))
  stdPrice = rep(NA, length(rounds))
  prices = files$stock0transactions$price
  for(round in rounds[2:length(rounds)]){
    idx = which(files$stock0transactions$round == round)
    mp = mean(files$stock0transactions$price[idx])
    sp = sd(files$stock0transactions$price[idx])
    if(is.nan(mp)){
      print(paste0(c("There were no transactions in round ", round, ", so the price was the same as last round."), collapse=""))
      meanPrice[round] = meanPrice[round-1]
      stdPrice[round] = stdPrice[round-1]
    } else{
      meanPrice[round] = mp
      stdPrice[round] = sp
    }
  }

  stdPrice = removeNaIdx(stdPrice)
  return(list('meanPrice'=meanPrice, 'stdPrice'=stdPrice, 'volume'=files$stock0roundBased$tradedVolume))
}


makeMeanPricePlot = function(files, rounds, meanPrice, stdPrice, from, to, SMAwindows){
  par(mfrow=c(2,1))
  maxPrice = max(meanPrice[from:to], files$stock0roundBased$fundamental[from:to])
  minPrice = min(meanPrice[from:to], files$stock0roundBased$fundamental[from:to])
  par(mai=c(0,1,1,1))
  plot(rounds[from:to], meanPrice[from:to], pch=19, cex=0.2, main=experimentName, sub=files$graphPrefix, ylim=c(minPrice, maxPrice), xaxt='n')
  lines(rounds[from:to], files$stock0roundBased$fundamental[from:to], col="red", lwd=2)
  lines(rounds[from:to], SMA(meanPrice[from:to], n=SMAwindows), col="green", lwd=2)
  #lines(SMA(files$stock0roundBased$fundamental[from:to]), col="green")
  text(x=median(rounds[from:to]), y=(max(meanPrice[from:to]) - min(meanPrice[from:to]))*0.9 + min(meanPrice[from:to]), labels=files$graphPrefix, cex=0.7, col="blue")
  #plot(SMA(files$stock0roundBased$tradedVolume[from:to],n=100), type="l")
  #par(mai=c(0,1,0,1))
  #plot(rounds[from:to], stdPrice[from:to], pch=19, cex=0.2)
  #lines(SMA(stdPrice, n=SMAwindows), type="l", col="red")
  par(mai=c(1,1,0,1))
  plot(rounds[from:to], SMA(files$stock0roundBased$tradedVolume[from:to],n=SMAwindows), type="l", col="red", lwd=2)
  
}

makeShortTimeTradePricePlot = function(files){
  par(mfrow=c(2,1))
  from = min(files$stock0transactions$round)
  to = max(files$stock0transactions$round)
  plot(files$stock0transactions$round[from:to], files$stock0transactions$price[from:to], ylim=c(9990, 10010), type="l")
  lines(files$stock0roundBased$fundamental, col="red", type="l", lwd=2)
  plot(files$stock0roundBased$tradedVolume, type="h")
  #from = min(which(files$stock0transactions$round == 12000))
  #to = max(which(files$stock0transactions$round == 15500))
  #plot(files$stock0transactions$round[from:to], files$stock0transactions$price[from:to], ylim=c(9990, 10010), type="l")
  #lines(files$stock0roundBased$fundamental, col="red", type="l", lwd=2)
  #from = min(which(files$stock0transactions$round == 15000))
  #to = max(which(files$stock0transactions$round == 15100))
  #plot(files$stock0transactions$round[from:to], files$stock0transactions$price[from:to], ylim=c(9990, 10010), type="l")
  #lines(files$stock0roundBased$fundamental, col="red", type="l", lwd=2)
  
}

removeNaIdx = function(a){
  naidx = which(is.na(a))
  a = a[-naidx]
  return(a)
}

files = loadFiles(logDir)
from = 1000
to = length(files$rounds)
SMAwindows = 200

#dataStats = getStats(files)
#makeMeanPricePlot(files, files$rounds, dataStats$meanPrice, data$stdPrice, from, to, SMAwindows)
makeShortTimeTradePricePlot(files=files)

#par(mfrow=c(2,1))
#smoothedMeanPrice = SMA(data$meanPrice[from:to], n=SMAwindows)
#smoothedMeanPrice = removeNaIdx(smoothedMeanPrice)
#plot(smoothedMeanPrice - min(smoothedMeanPrice), type="l")
#difference = diff(x=smoothedMeanPrice, lag=1)
#smoothedDiff = removeNaIdx(SMA(x=difference, n=200))
#plot(smoothedDiff, type="l", col="red")

#par(mfrow=c(1,1))
#hist(abs(difference), breaks=1000)

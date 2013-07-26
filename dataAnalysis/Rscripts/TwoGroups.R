rm(list=ls())

### For Import for SMA()

library(TTR)

### Import for postscript()
library(grDevices)

printPlots = FALSE

setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/TwoGroups/")
#setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/JAWS2013/usedData/smothCurves/")

figuresExportDir = "/Users/halfdan/Dropbox/Waseda/Research/JAWS2013/figures/"


getGroupWealthStatsAndMakePlots = function(agentsInGroup, makeFundamentalPlot = FALSE, makeNstocksPlot = FALSE, makeWealthPlot = FALSE, movingAverageWindowWidth = 50, figureCaption){
  ### Load stock data
  stock0roundBased = read.csv("columnLog_roundBased_stock0.cvs", sep=",")
  
  ### Make plot for fundamental
  if(makeFundamentalPlot){
    #par(mfrow=c(3,1), mar=c(2,2,2,1))
    plot(SMA(stock0roundBased$fundamental, n=movingAverageWindowWidth), type="l", main="Fundamental Price")
  }
  
  #########################################
  ### Load files for round based agent data
  file = paste0(c("columnLog_roundbased_agent_",agentsInGroup[1], ".cvs"), collapse="")
  variable = paste0(c("agent_", agentsInGroup[1], "_roundBased"), collapse="")
  assign(variable, read.csv(file=file))
  nOwnedStocksMatrixGroup = get(variable)$Stock0
  for(agent in agentsInGroup[-1]){
    file = paste0(c("columnLog_roundbased_agent_",agent, ".cvs"), collapse="")
    variable = paste0(c("agent_", agent, "_roundBased"), collapse="")
    assign(variable, read.csv(file=file))
    nOwnedStocksMatrixGroup = cbind(nOwnedStocksMatrixGroup, get(variable)$Stock0)
  }
  
  # Calculate min/max values
  groupMaxStock = max(max(nOwnedStocksMatrixGroup))
  groupMinStock = min(min(nOwnedStocksMatrixGroup))
  nOwnedStocksGroupMean = apply(X=nOwnedStocksMatrixGroup, MARGIN=1, FUN=mean)
  nOwnedStocksGroupStd = apply(X=nOwnedStocksMatrixGroup, MARGIN=1, FUN=sd)
  
  
  variable = paste0(c("agent_", agentsInGroup[1], "_roundBased"), collapse="")
  if(makeNstocksPlot){
    # Create plot by plotting first agent. 
    plot(SMA(get(variable)$Stock0, n=movingAverageWindowWidth), type="l", main=figureCaption, lwd=0.5, lty="dashed", ylim=c(groupMinStock, groupMaxStock))
    if(groupMinStock < 0){
      abline(0,0, lwd=2, lty="dashed")
    }
    
    # Plot the remaining agents in the same group.
    for(agent in agentsInGroup[-1]){
      variable = get(paste0(c("agent_", agent, "_roundBased"), collapse=""))
      lines(SMA(variable$Stock0, n=movingAverageWindowWidth), lty="dashed", lwd = 0.5)      
    }
    lines(SMA(nOwnedStocksGroupMean, n=movingAverageWindowWidth), col="red", lwd=2)
    lines(SMA(nOwnedStocksGroupMean+nOwnedStocksGroupStd, n=movingAverageWindowWidth), col="red", lwd=1, lty="dashed")
    lines(SMA(nOwnedStocksGroupMean-nOwnedStocksGroupStd, n=movingAverageWindowWidth), col="red", lwd=1, lty="dashed")
    legend(x=0, y=groupMaxStock, legend=c("Individual agents", "Group mean", "Group std"), col=c("black", "red", "red"), lty=c("dashed", "solid", "dashed"))
  }
  
  #####################################################
  ### Plotting portfolio value, cash and total for group
  
  # First, get data
  file = paste0(c("columnLog_roundbased_agent_",agentsInGroup[1], ".cvs"), collapse="")
  variable = paste0(c("agent_", agentsInGroup[1], "_roundBased"), collapse="")
  assign(variable, read.csv(file=file))
  cashMatrixGroup = get(variable)$cash
  portfolioMatrixGroup = get(variable)$portfolio
  for(agent in agentsInGroup[-1]){
    variable = get(paste0(c("agent_", agent, "_roundBased"), collapse=""))
    cashMatrixGroup = cbind(cashMatrixGroup, variable$cash)
    portfolioMatrixGroup = cbind(portfolioMatrixGroup, variable$portfolio)
  }
  
  # Calculate min/max values for plot
  groupMaxCash = max(max(cashMatrixGroup))
  groupMinCash = min(min(cashMatrixGroup))
  groupMaxPortfolio = max(max(portfolioMatrixGroup))
  groupMinPortfolio = min(min(portfolioMatrixGroup))
  cashGroupMean = apply(X=cashMatrixGroup, MARGIN=1, FUN=mean)
  cashGroupStd = apply(X=cashMatrixGroup, MARGIN=1, FUN=sd)
  portfolioGroupMean = apply(X=portfolioMatrixGroup, MARGIN=1, FUN=mean)
  portfolioGroupStd = apply(X=portfolioMatrixGroup, MARGIN=1, FUN=sd)
  totalWealthGroupMean = apply(X=cashMatrixGroup+portfolioMatrixGroup, MARGIN=1, FUN=mean)
  totalWealthGroupstd = apply(X=cashMatrixGroup+portfolioMatrixGroup, MARGIN=1, FUN=sd)
  plotmin = min(c(totalWealthGroupMean - totalWealthGroupstd, cashGroupMean))
  plotmax = max(totalWealthGroupMean + totalWealthGroupstd)
  
  if(makeWealthPlot){
    # Make plot
    plot(cashGroupMean, col="blue", lwd=2, type="l", ylim=c(plotmin, plotmax))
    lines(portfolioGroupMean, col="green", lwd=2)
    lines(totalWealthGroupMean, col="black", lwd=2)
    lines(totalWealthGroupMean + totalWealthGroupstd, col="black", lty="dashed", lwd=0.5)
    lines(totalWealthGroupMean - totalWealthGroupstd, col="black", lty="dashed", lwd=0.5)
    legend(x=0, y=plotmax, legend=c("Total wealth, group mean", "Total wealth, group std", "Portfolio value, group mean", "Cash, group mean"), col=c("black","black", "green", "blue"), lty=c("solid", "dashed", "solid", "solid"))
  }    
  
  ### Create list for returning values
  values = list("cashMean" = cashGroupMean, "cashStd" = cashGroupStd,
                "portofolioMean" = portfolioGroupMean, "portfolioStd" = portfolioGroupStd,
                "totalWealthMean" = totalWealthGroupMean, "totalWealthStd" = totalWealthGroupstd, 
                "nOwnedStocksMean" = nOwnedStocksGroupMean, "nOwnedStocksStd" = nOwnedStocksGroupStd)
  
  return(values)
}

if(printPlots){
  ### Create devide for printing figure
  setEPS()
  postscript(file=paste0(figuresExportDir,"experiment2_nStocks.eps"), width=7, height=6, horizontal=FALSE)
  #pdf(file=paste0(figuresExportDir,"experiment1_group1.pdf"), width=7, height=8)
}

stock0roundBased = read.csv("columnLog_roundBased_stock0.cvs", sep=",")
par(mfrow=c(2,1), mai=c(0.5,0.5,0.5,0.2), oma=c(0,0,0,0), cex=0.8)
agentsInGroup1 = 0:24
statsGroup1 = getGroupWealthStatsAndMakePlots(agentsInGroup=agentsInGroup1, makeNstocksPlot=TRUE, figureCaption="Number of owned stocks by fast HFTs")
agentsInGroup2 = 25:49
statsGroup2 = getGroupWealthStatsAndMakePlots(agentsInGroup=agentsInGroup2, makeNstocksPlot=TRUE, figureCaption="Number of owned stocks by slow HFTs")

if(printPlots){
  dev.off()
}


if(printPlots){
  ### Create devide for printing figure
  setEPS()
  postscript(file=paste0(figuresExportDir,"experiment2_StocksAndWealth.eps"), width=7, height=9, horizontal=FALSE)
  #pdf(file=paste0(figuresExportDir,"experiment1_StocksAndWealth.pdf"), width=7, height=8)
}


meanLineWidth = 2
stdLineWidth = 0.5
nWindows = 50
par(mfrow=c(4,1), mai=c(0.5,0.5,0.5,0.2), oma=c(0,0,0,0), cex =0.8)
plot(SMA(stock0roundBased$fundamental, n=nWindows), type="l", main="Fundamental price", xlab="", ylab="")


### Plot for fundamental price

if(FALSE){
  transactions = read.csv(file="columnLog_transactionBased_stock0.cvs", sep=",")
  maxPrice = max(c(stock0roundBased$fundamental, stock0roundBased$bestBuyAtMarket0, stock0roundBased$bestSellAtMarket0))
  minPrice = min(c(stock0roundBased$fundamental, stock0roundBased$bestBuyAtMarket0, stock0roundBased$bestSellAtMarket0))
  par(mfrow=c(1,1))
  
  maxPrice = max(c(stock0roundBased$fundamental, stock0roundBased$globalHighestBuyPrice, stock0roundBased$globalLowestSellPrice))
  minPrice = min(c(stock0roundBased$fundamental, stock0roundBased$globalHighestBuyPrice, stock0roundBased$globalLowestSellPrice))
  par(mfrow=c(1,1))
  plot(SMA(stock0roundBased$fundamental, n=nWindows), type="l", main="Fundamental price", xlab="", ylab="", lwd=meanLineWidth, ylim=c(minPrice, maxPrice))
  hftBuyerIdx = which(is.na(transactions$buyer) == TRUE)
  hftSellerIdx = which(is.na(transactions$seller) == TRUE)
  hftTradesWithHtfIdx = which(is.na(transactions$buyer) == TRUE & is.na(transactions$seller) == TRUE)
  points(x=transactions$round[hftBuyerIdx], y=transactions$price[hftBuyerIdx], pty=19, cex=0.1, col="purple")
  points(x=transactions$round[hftSellerIdx], y=transactions$price[hftSellerIdx], pty=19, cex=0.1, col="magenta")
  points(x=transactions$round[hftTradesWithHtfIdx], y=transactions$price[hftTradesWithHtfIdx], pty=19, cex=0.1, col="green")
  
  lines(SMA(stock0roundBased$globalHighestBuyPrice,n=nWindows), col="red", type="l")
  lines(SMA(stock0roundBased$globalLowestBuyPrice,n=nWindows), col="red", type="l", lty="dashed")
  lines(SMA(stock0roundBased$globalLowestSellPrice,n=nWindows), col="green", type="l")
  lines(SMA(stock0roundBased$globalHighestSellPrice,n=nWindows), col="green", type="l", lty="dashed")
  
  nTradesTotal = length(transactions$price)
  nTradesInvolvingOneHFTs = length(which(is.na(transactions$buyer) == TRUE | is.na(transactions$seller == TRUE)))
  nTradesInvolvingTwoHFTs = length(which(is.na(transactions$buyer) == TRUE & is.na(transactions$seller == TRUE)))
  nTradesInvolvingTwoSlowTraders = length(which(is.na(transactions$buyer) == FALSE & is.na(transactions$seller) == FALSE))
}

### Plot for number of owned stocks   
minNOwnedStocks = min(statsGroup1$nOwnedStocksMean - statsGroup1$nOwnedStocksStd, statsGroup2$nOwnedStocksMean - statsGroup2$nOwnedStocksStd)
maxNOwnedStocks = max(statsGroup1$nOwnedStocksMean + statsGroup1$nOwnedStocksStd, statsGroup2$nOwnedStocksMean + statsGroup2$nOwnedStocksStd)
plot(SMA(statsGroup1$nOwnedStocksMean, n=nWindows), type="l", col="black", lwd=meanLineWidth, ylim=c(minNOwnedStocks, maxNOwnedStocks), main="Mean number of owned stocks", xlab="", ylab="")
lines(SMA(statsGroup1$nOwnedStocksMean + statsGroup1$nOwnedStocksStd, n=nWindows), type="l", col="black", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup1$nOwnedStocksMean - statsGroup1$nOwnedStocksStd, n=nWindows), type="l", col="black", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup2$nOwnedStocksMean, n=nWindows), type="l", col="red")
lines(SMA(statsGroup2$nOwnedStocksMean + statsGroup2$nOwnedStocksStd, n=nWindows), type="l", col="red", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup2$nOwnedStocksMean - statsGroup2$nOwnedStocksStd, n=nWindows), type="l", col="red", lty="dashed", lwd = stdLineWidth)
if(minNOwnedStocks < 0){
  abline(a=0, b=0)
}
#legend(x=0, y=maxNOwnedStocks, legend=c("Fast HFTs", "Slow HFTs"), col=c("black", "red"), lty=c("solid", "solid"), , lwd=c(meanLineWidth, meanLineWidth))

### Plot for number of owned stocks   
mincash = min(statsGroup1$cashMean - statsGroup1$cashStd, statsGroup2$cashMean - statsGroup2$cashStd)
maxcash = max(statsGroup1$cashMean + statsGroup1$cashStd, statsGroup2$cashMean + statsGroup2$cashStd)
plot(SMA(statsGroup1$cashMean, n=nWindows), type="l", col="black", lwd=meanLineWidth, ylim=c(mincash, maxcash), main="Owned cash", xlab="", ylab="")
lines(SMA(statsGroup1$cashMean + statsGroup1$cashStd, n=nWindows), type="l", col="black", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup1$cashMean - statsGroup1$cashStd, n=nWindows), type="l", col="black", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup2$cashMean, n=nWindows), type="l", col="red")
lines(SMA(statsGroup2$cashMean + statsGroup2$cashStd, n=nWindows), type="l", col="red", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup2$cashMean - statsGroup2$cashStd, n=nWindows), type="l", col="red", lty="dashed", lwd = stdLineWidth)
if(mincash < 0){
  abline(a=0, b=0)
}
#legend(x=0, y=maxcash, legend=c("Fast HFTs", "Slow HFTs"), col=c("black", "red"), lty=c("solid", "solid"), , lwd=c(meanLineWidth, meanLineWidth))



minWealth = min(statsGroup1$totalWealthMean - statsGroup1$totalWealthStd, statsGroup2$totalWealthMean - statsGroup2$totalWealthStd)
maxWealth = max(statsGroup1$totalWealthMean + statsGroup1$totalWealthStd, statsGroup2$totalWealthMean + statsGroup2$totalWealthStd)
#plot(statsGroup1$stockFundamental)
plot(SMA(statsGroup1$totalWealthMean, n=nWindows), type="l", col="black", lwd=meanLineWidth, ylim=c(minWealth, maxWealth), main="Mean total wealth of groups", xlab="Round", ylab="")
lines(SMA(statsGroup1$totalWealthMean + statsGroup1$totalWealthStd, n=nWindows), type="l", col="black", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup1$totalWealthMean - statsGroup1$totalWealthStd, n=nWindows), type="l", col="black", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup2$totalWealthMean, n=nWindows), type="l", col="red")
lines(SMA(statsGroup2$totalWealthMean + statsGroup2$totalWealthStd, n=nWindows), type="l", col="red", lty="dashed", lwd = stdLineWidth)
lines(SMA(statsGroup2$totalWealthMean - statsGroup2$totalWealthStd, n=nWindows), type="l", col="red", lty="dashed", lwd = stdLineWidth)
if(minWealth < 0){
  abline(a=0, b=0)
  }
#legend(x=0, y=maxWealth, legend=c("Fast HFTs", "Slow HFTs"), col=c("black", "red"), lty=c("solid", "solid"), lwd=c(meanLineWidth, meanLineWidth))

if(printPlots){
  dev.off()
}
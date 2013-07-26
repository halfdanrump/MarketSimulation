library(TTR)
setwd(dir="/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/")
stock0roundBased = read.csv("columnLog_roundBased_stock0.cvs", sep=",")


####################################################
### Plot of fundamental price and best 
agent0roundBased = read.csv(file="columnLog_roundbased_agent_0.cvs", sep=",")
start = 1
end = 100
rounds = stock0roundBased$round[start:end]
par(mfrow=c(2,1), mar=c(2,2,2,1))
plot(rounds, stock0roundBased$fundamental[rounds], type="l", lwd=2, main="Global lowest and highest best buy/sell prices")
lines(rounds, stock0roundBased$globalHighestBuyPrice[rounds], col="green")
lines(rounds, stock0roundBased$globalLowestBuyPrice[rounds], col="green", lty="dashed")
lines(rounds, stock0roundBased$globalHighestSellPrice[rounds], col="blue", lty="dashed")
lines(rounds, stock0roundBased$globalLowestSellPrice[rounds], col="blue")
plot(rounds, stock0roundBased$smallestLocalSpread[start:end], col="red", type="h", main="Spread")

####################################################
### Plot of fundamental price and best for group experiment

agentsInGroup = 0:9
stock0roundBased = read.csv("columnLog_roundBased_stock0.cvs", sep=",")
start = 1
end = 100
windowWidth = 10
rounds = stock0roundBased$round[start:end]
par(mfrow=c(3,1), mar=c(2,2,2,1))
plot(SMA(stock0roundBased$fundamental, n=windowWidth), type="l", main="Fundamental Price")




getGroupWealthStatsAndMakePlots = function(agentsIngroup){
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
  groupMaxStock = max(max(nOwnedStocksMatrixGroup))
  groupMinStock = min(min(nOwnedStocksMatrixGroup))
  
  
  ###Create plot for number of owned stocks
  
  #Plotting number of owned stock for group 
  variable = paste0(c("agent_", agentsInGroup[1], "_roundBased"), collapse="")
  plot(get(variable)$Stock0, type="l", main="Number of owned stock", lwd=0.5, lty="dashed", ylim=c(groupMinStock, groupMaxStock))
  abline(0,0, lwd=2, lty="dashed")
  
  for(agent in agentsInGroup[-1]){
    variable = get(paste0(c("agent_", agent, "_roundBased"), collapse=""))
    lines(variable$Stock0, lty="dashed", lwd = 0.5)
    
  }
  groupMean = apply(X=nOwnedStocksMatrixGroup, MARGIN=1, FUN=mean)
  groupStd = apply(X=nOwnedStocksMatrixGroup, MARGIN=1, FUN=sd)
  lines(groupMean, col="green", lwd=3)
  lines(groupMean+groupStd, col="red", lwd=1)
  lines(groupMean-groupStd, col="red", lwd=1)
  
  #Plotting portfolio value, cash and total for group
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
  groupMaxCash = max(max(cashMatrixGroup))
  groupMinCash = min(min(cashMatrixGroup))
  groupMaxPortfolio = max(max(portfolioMatrixGroup))
  groupMinPortfolio = min(min(portfolioMatrixGroup))
  plotmax = max(groupMaxCash, groupMaxPortfolio)
  plotmin = min(groupMinCash, groupMinPortfolio)
  cashGroupMean = apply(X=cashMatrixGroup, MARGIN=1, FUN=mean)
  cashGroupStd = apply(X=cashMatrixGroup, MARGIN=1, FUN=sd)
  portfolioGroupMean = apply(X=portfolioMatrixGroup, MARGIN=1, FUN=mean)
  portfolioGroupStd = apply(X=portfolioMatrixGroup, MARGIN=1, FUN=sd)
  totalWealthGroupMean = apply(X=cashMatrixGroup+portfolioMatrixGroup, MARGIN=1, FUN=mean)
  totalWealthGroupstd = apply(X=cashMatrixGroup+portfolioMatrixGroup, MARGIN=1, FUN=sd)
  
  ###Create plot for number of owned stocks
  
  #Plotting number of owned stock for group 
  variable = get(paste0(c("agent_", agentsInGroup[1], "_roundBased"), collapse=""))
  plot(variable$cash, type="l", main="Value of portfolio, owned cash, and total", lwd=0.5, lty="dashed", ylim=c(plotmin, plotmax), col="blue")
  lines(variable$portfolio, type="l", lty="dashed", lwd=0.5, col="green")
  lines(variable$cash + variable$portfolio, type="l", lty="dashed", lwd=0.5, col="black")
  abline(0,0, lwd=2, lty="dashed")
  
  for(agent in agentsInGroup[-1]){
    variable = get(paste0(c("agent_", agent, "_roundBased"), collapse=""))
    lines(variable$cash, lty="dashed", lwd = 0.5, col="blue")
    lines(variable$portfolio, lty="dashed", lwd = 0.5, col="green")
    lines(variable$cash + variable$portfolio, lty="dashed", lwd = 0.5, col="black")
    
  }
  
  lines(cashGroupMean, col="green", lwd=4)
  lines(portfolioGroupMean, col="blue", lwd=4)
  lines(totalWealthGroupMean, col="black", lwd=4)
  
  values = list("cashMean" = cashGroupMean, "cashStd" = cashGroupStd,
                "portofolioMean" = portfolioGroupMean, "portfolioStd" = portfolioGroupStd,
                "totalWealthMean" = totalWealthGroupMean, "totalWealthStd" = totalWealthGroupstd)
  
  return(values)
}

group1 = getG












for(agent in 10:19){
  
}
  
  plot(SMA(agent0roundBased$portfolio, n=windowWidth), type="l", col="green", lwd=1, main="Portfolio and cash")
  lines(SMA(agent0roundBased$cash, n=windowWidth), lwd=1, col="blue")
  abline(0,0, lwd=2, lty="dashed")
  
}





windowWidth = 10
par(mfrow=c(3,1), mar=c(2,2,2,1))
plot(SMA(stock0roundBased$fundamental, n=windowWidth), type="l", main="Fundamental Price")
plot(SMA(agent0roundBased$Stock0, n=windowWidth), type="l", main="Number of owned stock")
abline(0,0, lwd=2, lty="dashed")
plot(SMA(agent0roundBased$portfolio, n=windowWidth), type="l", col="green", lwd=1, main="Portfolio and cash")
lines(SMA(agent0roundBased$cash, n=windowWidth), lwd=1, col="blue")
abline(0,0, lwd=2, lty="dashed")



#Making plots for 
start = 1
end = 100


agent0tradelog = read.csv(file="columnLog_tradelog_agent_0.cvs", sep=",")
buyIdx = grep("BUY", agent0tradelog$buysell)
sellIdx = grep("SELL", agent0tradelog$buysell)
plot(stock0$round[start:end], stock0$fundamental[start:end], type="l", lwd=2)
lines(stock0$round[start:end], stock0$bBuy0[start:end], col="green")
lines(stock0$round[start:end], stock0$bSell0[start:end], col="blue")
points(buyIdx[start:end], agent0tradelog$price[buyIdx[start:end]], col="green", pch=19, cex=0.5)
points(sellIdx, agent0tradelog$price[sellIdx], col="blue", pch=19, cex=0.5)
plot(buyIdx[start:end], agent0tradelog$price[buyIdx[start:end]], col="green", pch=19, cex=0.5)
find(agent0tradelog$buysell=="BUY")

find(what=)
agent0$Stock0

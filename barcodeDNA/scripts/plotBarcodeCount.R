#!/usr/bin/env Rscript
#
# Plot barcode DNA count
#
# $ plotBarcodeCount.R 201B7-05-1_S7_L001_R1_001.insertFull.count "201B7-05-1_R1 DNA barcode count" 201B7-05-1_R1 [eps|svg|png|pdf]
# % R --vanilla --quiet --args  < ../Rscripts/plotBarcodeCount.R
# % R CMD BATCH plotBarcodeCount.R
# #source("countSegmentBarPlot.R")
#
#library(MASS)
#
# double y plot
#
plotBarcodeCounts <- function( countP, ymax, titleHead, outHead, subType, outType ) {
  nT <- ncol(countP) - 1
  cT <- colnames(countP)[2:ncol(countP)]
  cR <- rev(rainbow(nT+2)[1:nT])
  cP <- 1:length(cT)
  cS <- 1.0
  nRow <- ncol(countP)-1
  nCol <- nrow(countP)
  xticks <- rep( (nRow+1)*(1:nCol - 1) ) + nRow/2+1
  xLab <- "Barcode DNA"
  yLab <- "Number of Hits"
  lePos <- "topleft"
  outName <- paste( outHead, subType, outType, sep="." )
  title <- paste0( titleHead, ": ", subType )
  #
  w <- 18
  denom <- 8.5
  leY <- ymax*1.25
  if ( outType == "eps" | outType == "EPS" ) {
    postscript( outName, paper='special', height=5, width=w, horizontal=F )
  } else if ( outType == "svg" | outType == "SVG" ) {
    svg( outName, height=5, width=w )
  } else if ( outType == "png" | outType == "PNG" ) {
    leY <- ymax*1.15
    png( outName, height=500, width=(w*100) )
  } else if ( outType == "pdf" | outType == "PDF" ) {
    pdf( outName, height=5, width=w )
  } else {
    print( paste("Unknown output type:", outName) )
    q("no")
  }
  barplot( t(countP[,2:ncol(countP)]), ylab=yLab, ylim=c(0,ymax), main=title,
           col=cR, beside=T, names.arg=rep("", nCol),
	   legend.text=cT, args.legend=list(x=xticks[2],y=leY) ) # list(x=lePos)
  #abline(a=0, b=0, col="black")
  #
  par(xpd=TRUE)
  yPos <- - ymax/denom
#  legend( lePos, legend=cT, fil=cR, cex=1.0, bty="n" )
  text( xticks, y=yPos, labels=countP[,1], cex=0.8 ) #, srt=90
  dev.off()
}
#[1] "/usr/local/lib64/R/bin/exec/R"  "--slave"                       
#[3] "--no-restore"                   "--file=plotCNVcount.R"
#[5] "--args"                         "*.count"
#[7] "title"                          "output-prefix"
args <- commandArgs();
i <- 5
if ( regexpr("--file=", args[4]) > 0 ) {
   i <- 6
}
#
#counts <- read.csv( args[i], comment.char = "#", header=TRUE )
counts <- read.table( args[i], comment.char = "#", header=TRUE )
titleHead <- args[i+1]
outHead <- args[i+2]
outType <- "eps"
if ( length(args) > i+2 ) {
   outType <- tolower(args[i+3])
}
#counts <- read.table( "201B7-05-1_S7_L001_R1_001.insertFull.count.csv", header=TRUE )
#titleHead <- "201B7-05-1 DNA barcode count"
#outHead <- "201B7-05-1_R1"
#
# colnames(counts)
# [1] "BarcodeID"         "BarcodeSeq"        "BarcodeRevCompSeq"
# [4] "SeqCountM0F0"      "RevCountM0F0"      "BothCount0F0"     
# [7] "SeqProp0F0"        "RevProp0F0"        "BothProp0F0"      
#[10] "SeqCountM0F1"      "RevCountM0F1"      "BothCount0F1"     
#[13] "SeqProp0F1"        "RevProp0F1"        "BothProp0F1"      
#[16] "SeqCountM0F2"      "RevCountM0F2"      "BothCount0F2"     
#[19] "SeqProp0F2"        "RevProp0F2"        "BothProp0F2"      
#[22] "SeqCountM1F0"      "RevCountM1F0"      "BothCount1F0"     
#[25] "SeqProp1F0"        "RevProp1F0"        "BothProp1F0"      
#[28] "SeqCountM1F1"      "RevCountM1F1"      "BothCount1F1"     
#[31] "SeqProp1F1"        "RevProp1F1"        "BothProp1F1"      
#[34] "SeqCountM1F2"      "RevCountM1F2"      "BothCount1F2"     
#[37] "SeqProp1F2"        "RevProp1F2"        "BothProp1F2"
#
ymax <- max(counts[,seq(6,ncol(counts),6)], na.rm=T)
plotBarcodeCounts( counts[,c(1,seq(4,ncol(counts),6))], ymax, titleHead, outHead, "SeqCount", outType )
plotBarcodeCounts( counts[,c(1,seq(5,ncol(counts),6))], ymax, titleHead, outHead, "RevCount", outType )
plotBarcodeCounts( counts[,c(1,seq(6,ncol(counts),6))], ymax, titleHead, outHead, "BothCount", outType )

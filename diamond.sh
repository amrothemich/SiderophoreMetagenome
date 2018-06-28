#!/bin/sh
diamond makedb --in ReferenceGenes/coli40.fasta -d ReferenceGenes/coli40db
wait

mkdir diamond
mkdir diamond/coli40

FILES=contigs/*
for f in $FILES
do
    FILESHORT=${f%:*}
    FILESHORT=${f##*/}
    FILESHORT=${FILESHORT%%_contigs.fna}
    echo $f
    diamond blastx -d ReferenceGenes/coli40db -q $f -o Diamond/coli40/$FILESHORT
done

diamond makedb --in ReferenceGenes/coliCFT073.fasta -d ReferenceGenes/coliCFT073db
wait 

mkdir Diamond/coliCFT073

FILES=contigs/*
for f in $FILES
do
    FILESHORT=${f%:*}
    FILESHORT=${f##*/}
    FILESHORT=${FILESHORT%%_contigs.fna}
    echo $f
    diamond blastx -d ReferenceGenes/coliCFT073db -q $f -o Diamond/coliCFT073/$FILESHORT
done
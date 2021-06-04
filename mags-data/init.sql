
insert into precomputed_mags(accession, diso, abd, csl, "int", len, "max", phs, pip, rna, mrf, lps, cat, a, c, d, e, f, g, h, i, k, l, m, n, p, q, r, s, t, v, w, y)
select "name", diso, abd, csl, "int", len, "max", phs, pip, rna, mrf, lps, cat, a, c, d, e, f, g, h, i, k, l, m, n, p, q, r, s, t, v, w, y  from precomp_human_data_tsv;

UPDATE precomputed_mags set species='HUMAN';

insert into precomputed_mags(accession,diso,abd,csl,"int",len,"max",phs,pip,rna,mrf,lps,cat,tgo,gvy,a,c,d,e,f,g,h,i,k,l,m,n,p,q,r,s,t,v,w,y) 
select "name",diso,abd,csl,"int",len,"max",phs,pip,rna,mrf,lps,cat,tgo,gvy,a,c,d,e,f,g,h,i,k,l,m,n,p,q,r,s,t,v,w,y from precomp_yeast_data_tsv;

UPDATE precomputed_mags set species='YEAST' where species is NULL;

update precomputed_mags set z_score = "avg" from precomp_human_zs_tsv where precomp_human_zs_tsv.nam = precomputed_mags.accession;
update precomputed_mags set z_score = "avg" from precomp_yeast_zs_tsv where precomp_yeast_zs_tsv."name" = precomputed_mags.accession;

--  gene names

update precomputed_mags SET gene = "human_uni2genesyn.txt".gene from "human_uni2genesyn.txt" where "human_uni2genesyn.txt".accession=precomputed_mags.accession;
update precomputed_mags SET gene = "yeast_gene.txt".gene from "yeast_gene.txt" where "yeast_gene.txt".accession=precomputed_mags.accession;

--  magseq

insert into precomputed_magsseq(accession,diso,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w) 
select nam,dis,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w  from tmp_seq_hmn;

UPDATE precomputed_magsseq set species='HUMAN';

insert into precomputed_magsseq(accession,diso,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,sft,scn,sbb,pol,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w) 
select nam,dis,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,sft,scn,sbb,pol,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w from tmp_seq_yst;

UPDATE precomputed_magsseq set species='YEAST' where species is NULL;

-- Scores

update precomputed_magsseq set z_score_human = avg from tmp_scores_hmn where tmp_scores_hmn.nam = precomputed_magsseq.accession;
update precomputed_magsseq set z_score_yeast = avg from tmp_scores_yst where tmp_scores_yst.nam = precomputed_magsseq.accession;

--  gene names
update precomputed_magsseq SET gene = "human_uni2genesyn.txt".gene from "human_uni2genesyn.txt" where "human_uni2genesyn.txt".accession=precomputed_magsseq.accession;
update precomputed_magsseq SET gene = "yeast_gene.txt".gene from "yeast_gene.txt" where "yeast_gene.txt".accession=precomputed_magsseq.accession;

-- Markers

INSERT INTO marker_mags (accession, label) VALUES
    ('Q12517', 'p-body (DCP1)'),
    --    ('Q9NPI6', 'p-body (DCP1)'),
    ('Q8IU60', 'p-body (DCP2)'),
    ('P53550', 'p-body (DCP2)'),
    ('P04147', 'Stress Granule (PAB1)'),
    ('P11940', 'Stress Granule (PAB1)'),
    ('Q13283', 'Stress Granule (G3BP1)');


INSERT INTO marker_magseq (accession, label) VALUES
('Q12517', 'p-body (DCP1)'),
('Q9NPI6', 'p-body (DCP1)'),
('Q8IU60', 'p-body (DCP2)'),
('P53550', 'p-body (DCP2)'),
('P04147', 'Stress Granule (PAB1)'),
('P11940', 'Stress Granule (PAB1)'),
('Q13283', 'Stress Granule (G3BP1)');

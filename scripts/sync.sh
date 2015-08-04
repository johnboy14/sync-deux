#Delete the contents of the copy in preparation for comparing local with remote
cd && rm -rf congress/bills/114/copy/*
cd && rm -rf congress/votes/114/copy/*

#Use Rsync to retrieve files for congress 114
cd && rsync -avz --delete --delete-excluded --exclude **/text-versions/ --exclude **data.xml  govtrack.us::govtrackdata/congress-legislators/legislators-current.yaml congress/114/legislators/

#Compare whats in the Remote drive first against local and populate the copy directory
cd && rsync -avz --delete --delete-excluded --exclude **/text-versions/ --exclude **data.xml --compare-dest=../congress/bills/114/local govtrack.us::govtrackdata/congress/114/bills/ congress/bills/114/copy/
cd && rsync -avz --delete --delete-excluded --exclude **/text-versions/ --exclude **data.xml --compare-dest=../congress/votes/114/local govtrack.us::govtrackdata/congress/114/votes/ congress/votes/114/copy/

#Sync whats in the remote with the local copy in preparation for the next sync job
cd && rsync -avz --delete --delete-excluded --exclude **/text-versions/ --exclude **data.xml  govtrack.us::govtrackdata/congress/114/bills/ congress/bills/114/local/
cd && rsync -avz --delete --delete-excluded --exclude **/text-versions/ --exclude **data.xml  govtrack.us::govtrackdata/congress/114/votes/ congress/votes/114/local/
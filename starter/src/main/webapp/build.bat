cd packages/local/chemistry
sencha package build
cd ../../../

cd entry
sencha app build development
cd ..

cd admin
sencha app build development
cd ..

cd explorer
sencha app build development
cd ..

cd monitor
sencha app build development
cd ..


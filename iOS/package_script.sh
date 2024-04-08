
echo $1
echo $2

branch=${1//\//_}
echo branch: $branch

repo_name=$1

http_proxy=http://10.10.114.51:1080 https_proxy=http://10.10.114.51:1080 pod install --repo-update
if [ $? -eq 0 ]; then
    echo "pod success"
else
    echo "pod failed"
    exit 1
fi

script_path="$( cd "$(dirname "$0")" ; pwd -P )"
echo $script_path
today=$(date +"%Y-%m-%d")
echo $repo_name
zip -r ${repo_name}_${branch}_${today}.zip .
#!/usr/bin/env bash
# 명령어 실패 시 즉시 종료
set -e

echo "1. 작업 디렉토리로 이동: ~/Loan-Mate-Backend/docker"
cd ~/Loan-Mate-Backend/docker

echo "2. Git 최신 develop 브랜치로 Hard Reset 수행"
git fetch origin develop
git reset --hard origin/develop

echo "3. Docker 이미지 재빌드 및 서비스 재시작"
docker-compose up -d --build

echo "4. 사용되지 않는 Docker 이미지 정리"
docker image prune -f

echo "배포 완료!"
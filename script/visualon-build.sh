#!/bin/bash

SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../
VO_ZIP_BASE="OoyalaVisualOnIntegration-${PLATFORM_NAME}"
VO_ZIP_NAME=${VO_ZIP_BASE}.zip


function gen_vo {
  echo "Building VisualOn zip"
  cp ${BASE_DIR}/${ZIP_BASE}/${JAR_NAME} ${BASE_DIR}/third_party_sample_apps/VisualOnSampleApp/libs/

  cd ${BASE_DIR}
  mkdir ${VO_ZIP_BASE}

  version=$(get_version)
  saved_rc=$(get_rc)
  git_rev=`git rev-parse HEAD`
  #VisualOn version file
  echo "This was built with OoyalaSDK v${version}_RC${saved_rc}" >> ${VO_ZIP_BASE}/VERSION
  echo "Git SHA: ${git_rev}" >> ${VO_ZIP_BASE}/VERSION
  echo "Created On: ${DATE}" >> ${VO_ZIP_BASE}/VERSION

  mkdir ${VO_ZIP_BASE}/libs
  mkdir ${VO_ZIP_BASE}/libs/armeabi-v7a
  cp -r ${BASE_DIR}/vendor/VisualOn/Assets ${BASE_DIR}/${VO_ZIP_BASE}/assets
  cp ${BASE_DIR}/vendor/VisualOn/Libs/* ${BASE_DIR}/${VO_ZIP_BASE}/libs/armeabi-v7a
  cp ${BASE_DIR}/vendor/VisualOn/Jar/* ${BASE_DIR}/${VO_ZIP_BASE}/libs
  cp -r ${BASE_DIR}/vendor/VisualOn/HOW_TO_INTEGRATE_WITH_VISUALON.txt ${BASE_DIR}/${VO_ZIP_BASE}/
  cp -r ${BASE_DIR}/third_party_sample_apps/VisualOnSampleApp ${BASE_DIR}/${VO_ZIP_BASE}/

  rm ${VO_ZIP_NAME}
  zip -r ${VO_ZIP_BASE} ${VO_ZIP_BASE}/*
  rm -rf ${VO_ZIP_BASE}
}

function pub_rc_vo {
  echo "Publishing VisualOn Release Candidate"
  echo "  Copying ${VO_ZIP_NAME} to ${CANDIDATE_DIR}${VO_ZIP_NAME}"
  cp ${VO_ZIP_NAME} "${CANDIDATE_DIR}"${VO_ZIP_NAME}
}

function pub_release_vo {
  echo "Moving VisualOn RC to Release"
  echo "  Copying ${CANDIDATE_DIR}${VO_ZIP_NAME} to ${RELEASE_DIR}${VO_ZIP_NAME}"
  cp "${CANDIDATE_DIR}"${VO_ZIP_NAME} "${RELEASE_DIR}"${VO_ZIP_NAME}
}
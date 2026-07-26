import { useLayoutEffect } from "react";

function useHideMainHeader() {
  useLayoutEffect(function () {
    const header = document.querySelector(".header-container-fluid");

    if (!header) {
      return undefined;
    }

    const wasHidden = header.hidden;
    header.hidden = true;

    return function () {
      header.hidden = wasHidden;
    };
  }, []);
}

export default useHideMainHeader;

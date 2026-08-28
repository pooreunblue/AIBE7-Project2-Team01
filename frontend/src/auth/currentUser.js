import { apiRequest } from "../api/api.js";

let currentUser = null;
let currentUserPromise = null;

export async function getCurrentUser({ force = false, optional = false } = {}) {
  if (currentUser && !force) return currentUser;
  if (currentUserPromise && !force) return currentUserPromise;

  currentUserPromise = apiRequest("/users/me", { authOptional: optional })
    .then((response) => {
      currentUser = response?.data ?? response;
      return currentUser;
    })
    .catch((error) => {
      currentUser = null;
      if (!optional) throw error;
      return null;
    })
    .finally(() => {
      currentUserPromise = null;
    });

  return currentUserPromise;
}

export async function getCurrentUserId(options) {
  const user = await getCurrentUser(options);
  return user?.userId ?? null;
}

export async function getCurrentUserEmail(options) {
  const user = await getCurrentUser(options);
  return user?.email ?? null;
}

export function getCachedCurrentUser() {
  return currentUser;
}

export function clearCurrentUser() {
  currentUser = null;
  currentUserPromise = null;
}

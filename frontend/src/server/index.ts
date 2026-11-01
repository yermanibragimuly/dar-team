import axios from "axios";
import { API_BASE_URL } from "@/shared/service/api"

export const BaseApi = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
});
